package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Animation;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.IdentityHashMap;

/**
 * Vape/Respect-style ClickGUI for Jay Utility Client.
 *
 * Layout mirrors the reference design: every category is its own floating,
 * draggable panel with a header accent underline, item-count badge and
 * hover-bright outline; a fixed sidebar with animated selection bar focuses
 * or folds panels; a top bar hosts the logo, global search and the accent
 * preset swatch strip. Module rows carry animated status bars, keybind chips
 * and inline settings — mini switches for toggles, cyclic value chips for
 * modes and label/value + track sliders for numbers.
 *
 * All motion is frame-rate independent ease-out in the 140–260 ms range and
 * uses only the 1.21.11 Matrix3x2fStack push/translate/scale/pop surface.
 */
public class ClickGuiScreen extends Screen {

    // ------------------------------------------------------------- layout
    private static final int SIDEBAR_W = 66;
    private static final int PANEL_W = 108;
    private static final int HEADER_H = 17;
    private static final int ROW_H = 15;
    private static final int SETTING_H = 13;
    private static final int MAX_ROWS = 12;
    private static final long HOLD_MS = 450;

    // preset swatches (mirrors GuiColors.applyPreset)
    private static final String[] PRESET_NAMES = {
            "purple", "blue", "red", "green", "orange", "pink", "gold", "white"
    };
    private static final int[] PRESET_COLORS = {
            0x9B6BFF, 0x3DDCFF, 0xFF5555, 0x55FF88, 0xFFAA33, 0xFF6BCB, 0xFFC84A, 0xE8E8F0
    };

    // panel window state (all categories visible, draggable)
    private final Map<Module, Boolean> expanded = new IdentityHashMap<>();
    private final java.util.Set<Module.Category> folded = java.util.EnumSet.noneOf(Module.Category.class);
    private final Map<Module.Category, Integer> scroll = new EnumMap<>(Module.Category.class);
    private final List<Module.Category> drawOrder = new ArrayList<>();
    private Module.Category dragCat;
    private boolean panelDragged;
    private double dragOx, dragOy;

    // top bar / search / accent strip
    private String search = "";
    private boolean searchFocused;
    private boolean presetStripOpen;

    // hold-to-bind
    private Module holdModule;
    private long holdStart;
    private boolean bindingMode;
    private Module bindingModule;
    private long bindHintUntil;
    private Module bindHintModule;

    // widgets
    private NumberSetting draggingSlider;
    private final Map<String, Long> hoverAt = new java.util.HashMap<>();
    private final Map<Setting, Float> toggleAnim = new IdentityHashMap<>();

    // animation
    private final Animation openAnim = new Animation(200, 1.0, Animation.Easing.EASE_OUT_CUBIC);
    private final Map<Module.Category, Float> sidebarSel = new EnumMap<>(Module.Category.class);
    private final Map<Module, Float> expandAnim = new IdentityHashMap<>();

    // hit boxes (rebuilt each render frame, used by input)
    private int searchBoxX, searchBoxY, searchBoxW, searchBoxH;
    private int accentX = -1;
    private int stripY = -1;

    public ClickGuiScreen() {
        super(Text.literal("Jay Client"));
    }

    @Override
    protected void init() {
        super.init();
        openAnim.reset();
        openAnim.setDirection(Animation.Direction.FORWARDS);
        try { PremiumTheme.onGuiOpen(); } catch (Throwable ignored) {}
        if (drawOrder.isEmpty()) {
            for (Module.Category c : Module.Category.values()) drawOrder.add(c);
        }
    }

    @Override
    public void close() {
        try { PremiumTheme.onGuiClose(); } catch (Throwable ignored) {}
        super.close();
    }

    // ------------------------------------------------------------- helpers

    private float guiScale() {
        return Math.max(0.85f, Math.min(1.25f, ClientSettings.guiScale));
    }

    private int rowH() { return Math.round(ROW_H * guiScale()); }
    private int setH() { return Math.round(SETTING_H * guiScale()); }
    private int panelW() { return Math.round(PANEL_W * guiScale()); }
    private int sidebarW() { return Math.round(SIDEBAR_W * guiScale()); }

    private int sidebarX() { return 10; }
    private int sidebarY() { return 10; }
    private int sidebarH() { return Module.Category.values().length * Math.round(18 * guiScale()) + 2; }

    private List<Module> modulesIn(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search.toLowerCase(Locale.ROOT).trim();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getCategory() != cat) continue;
            if (!q.isEmpty() && !m.getSearchBlob().contains(q)) continue;
            out.add(m);
        }
        out.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return out;
    }

    private int settingRows(Module m) {
        int n = 0;
        for (Setting s : m.getSettings()) {
            n += (s instanceof NumberSetting) ? 2 : 1;
        }
        return n;
    }

    private boolean isExpanded(Module m) {
        return Boolean.TRUE.equals(expanded.get(m));
    }

    private float expandProgress(Module m) {
        float target = isExpanded(m) ? 1f : 0f;
        float cur = expandAnim.getOrDefault(m, target);
        float next = cur + (target - cur) * 0.28f; // ~180ms at 60fps
        if (Math.abs(next - target) < 0.01f) next = target;
        expandAnim.put(m, next);
        return next;
    }

    private float togglePos(Setting s, boolean on) {
        float cur = toggleAnim.getOrDefault(s, on ? 1f : 0f);
        float next = cur + ((on ? 1f : 0f) - cur) * 0.35f;
        if (Math.abs(next - (on ? 1f : 0f)) < 0.02f) next = on ? 1f : 0f;
        toggleAnim.put(s, next);
        return next;
    }

    /** 0..1 hover ramp used for smooth row highlight (~140ms). */
    private float hoverRamp(String key, boolean hovering) {
        long start = hoverAt.getOrDefault(key, hovering ? System.currentTimeMillis() : 0L);
        if (!hovering) {
            if (hoverAt.containsKey(key)) hoverAt.put(key, 0L);
            return 0f;
        }
        if (start == 0L) { start = System.currentTimeMillis(); hoverAt.put(key, start); }
        float t = (System.currentTimeMillis() - start) / 140f;
        return MathHelper.clamp(t, 0f, 1f);
    }

    private String safeKeyLabel(Module m) {
        String k = m.getKeyLabel();
        return (k == null || k.isEmpty()) ? "" : k;
    }

    private void saveQuiet() {
        try { if (JayHackClient.configManager != null) JayHackClient.configManager.save(); } catch (Throwable ignored) {}
    }

    private boolean panelVisible() { return !drawOrder.isEmpty(); }

    private void bringToFront(Module.Category cat) {
        drawOrder.remove(cat);
        drawOrder.add(cat);
    }

    // panel geometry ------------------------------------------------------

    private int panelX(Module.Category cat) {
        float[] pos = GuiLayout.get(cat);
        return Math.max(4, (int) pos[0]);
    }

    private int panelY(Module.Category cat) {
        float[] pos = GuiLayout.get(cat);
        return Math.max(4, (int) pos[1]);
    }

    private boolean panelCollapsed(Module.Category cat) {
        return folded.contains(cat);
    }

    private void setPanelCollapsed(Module.Category cat, boolean fold) {
        if (fold) folded.add(cat); else folded.remove(cat);
    }

    /** Total rendered height of a panel (header + rows + expanded settings). */
    private int panelHeight(Module.Category cat) {
        if (panelCollapsed(cat)) return HEADER_H;
        List<Module> list = modulesIn(cat);
        int vis = Math.min(list.size(), MAX_ROWS);
        int h = HEADER_H + 2 + vis * rowH();
        for (int i = 0; i < vis; i++) {
            Module m = list.get(i);
            if (isExpanded(m) && !m.getSettings().isEmpty()) {
                h += Math.round(settingRows(m) * setH() * expandProgress(m));
            }
        }
        return h;
    }

    private boolean insidePanel(Module.Category cat, double mx, double my) {
        int px = panelX(cat), py = panelY(cat), pw = panelW();
        int ph = panelHeight(cat);
        return mx >= px && mx <= px + pw && my >= py && my <= py + ph;
    }

    // ------------------------------------------------------------- render

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        float anim = openAnim.getOutputF();
        if (anim < 0.02f) return;

        // dim backdrop
        ctx.fill(0, 0, width, height, RenderUtil.withAlpha(0x000000, 0.45f * anim));

        ctx.getMatrices().pushMatrix();
        float cx = width / 2f, cy = height / 2f;
        float s = 0.95f + 0.05f * anim;
        ctx.getMatrices().translate(cx, cy);
        ctx.getMatrices().scale(s, s);
        ctx.getMatrices().translate(-cx, -cy);

        int mx = mouseX, my = mouseY;

        Module.Category top = topCategoryAt(mx, my);

        // panels below sidebar (topmost last for correct overlap)
        for (Module.Category cat : drawOrder) {
            if (cat != top) drawPanel(ctx, cat, mx, my);
        }
        if (top != null) drawPanel(ctx, top, mx, my);

        drawSidebar(ctx, mx, my);
        drawTopBar(ctx, mx, my);
        if (presetStripOpen) drawPresetStrip(ctx, mx, my);

        if (bindingMode && bindingModule != null) {
            String msg = "Press a key to bind " + bindingModule.getName() + "  (ESC cancels)";
            int w = textRenderer.getWidth(msg) + 12;
            int bx = width / 2 - w / 2, by = height - 30;
            RenderUtil.drawRoundedRect(ctx, bx, by, w, 16, 4f, VapeTheme.BG_HEADER);
            ctx.drawTextWithShadow(textRenderer, msg, bx + 6, by + 4, VapeTheme.ACCENT());
        } else if (System.currentTimeMillis() < bindHintUntil && bindHintModule != null) {
            String msg = "Bound " + bindHintModule.getName() + " to " +
                    (safeKeyLabel(bindHintModule).isEmpty() ? "none" : safeKeyLabel(bindHintModule));
            int w = textRenderer.getWidth(msg) + 12;
            RenderUtil.drawRoundedRect(ctx, width / 2 - w / 2, height - 30, w, 16, 4f, VapeTheme.BG_HEADER);
            ctx.drawTextWithShadow(textRenderer, msg, width / 2 - w / 2 + 6, height - 26, VapeTheme.TEXT());
        }

        ctx.getMatrices().popMatrix();
    }

    private Module.Category topCategoryAt(int mx, int my) {
        for (int i = drawOrder.size() - 1; i >= 0; i--) {
            Module.Category c = drawOrder.get(i);
            if (insidePanel(c, mx, my)) return c;
        }
        return null;
    }

    private void drawTopBar(DrawContext ctx, int mx, int my) {
        int barH = Math.round(18 * guiScale());
        int x = sidebarX(), y = 2;
        int w = sidebarW() + panelW() + 8;

        RenderUtil.drawRoundedRect(ctx, x + 1, y + 1, w, barH, 3f, RenderUtil.withAlpha(0x000000, 0.4f));
        RenderUtil.drawRoundedRect(ctx, x, y, w, barH, 3f, VapeTheme.BG_HEADER);
        RenderUtil.drawRect(ctx, x, y + barH - 1, w, 1, RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.55f));

        try { JayLogo.draw(ctx, x + 4, y + 3, 12); } catch (Throwable ignored) {}
        ctx.drawTextWithShadow(textRenderer, "§dJAY§f CLIENT §8· §7" + JayHackClient.VERSION,
                x + 20, y + 5, VapeTheme.TEXT());

        // search box
        int sbW = Math.round(96 * guiScale());
        int sbX = x + w - sbW - 4;
        searchBoxX = sbX; searchBoxY = y + 2; searchBoxW = sbW; searchBoxH = barH - 4;
        boolean sbHover = mx >= sbX && mx <= sbX + sbW && my >= searchBoxY && my <= searchBoxY + searchBoxH;
        RenderUtil.drawRect(ctx, sbX, searchBoxY, sbW, searchBoxH, VapeTheme.BG_MODULE);
        if (searchFocused || sbHover) {
            RenderUtil.drawRect(ctx, sbX, searchBoxY, sbW, 1, searchFocused ? VapeTheme.ACCENT()
                    : RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.4f));
        }
        String shown = searchFocused ? search + "§d|" : (search.isEmpty() ? "§8search…" : search);
        ctx.drawTextWithShadow(textRenderer, shown, sbX + 4, y + 5, VapeTheme.TEXT());

        // accent swatch (opens preset strip)
        accentX = sbX - 16;
        boolean swHover = mx >= accentX && mx <= accentX + 12 && my >= y + 3 && my <= y + barH - 3;
        RenderUtil.drawRect(ctx, accentX, y + 3, 12, barH - 6, VapeTheme.BG_MODULE);
        RenderUtil.drawRect(ctx, accentX + 2, y + 5, 8, barH - 10, GuiColors.accent);
        if (swHover || presetStripOpen) {
            RenderUtil.drawRect(ctx, accentX, y + 3, 12, 1, VapeTheme.ACCENT());
            RenderUtil.drawRect(ctx, accentX, y + barH - 4, 12, 1, VapeTheme.ACCENT());
        }
    }

    private void drawPresetStrip(DrawContext ctx, int mx, int my) {
        if (accentX < 0) return;
        int sw = 12, gap = 3;
        int n = PRESET_NAMES.length;
        int w = n * (sw + gap) - gap;
        int x = Math.min(accentX - w + 12, width - w - 4);
        int y = stripY = searchBoxY + searchBoxH + 3;

        RenderUtil.drawRoundedRect(ctx, x + 1, y + 1, w, sw + 6, 3f, RenderUtil.withAlpha(0x000000, 0.4f));
        RenderUtil.drawRoundedRect(ctx, x, y, w, sw + 6, 3f, VapeTheme.BG_HEADER);
        for (int i = 0; i < n; i++) {
            int sx = x + 3 + i * (sw + gap);
            int sy = y + 3;
            boolean hover = mx >= sx && mx <= sx + sw && my >= sy && my <= sy + sw;
            boolean active = GuiColors.presetName().equals(PRESET_NAMES[i]);
            RenderUtil.drawRect(ctx, sx, sy, sw, sw, PRESET_COLORS[i]);
            if (hover || active) {
                int oc = active ? 0xFFFFFFFF : VapeTheme.ACCENT();
                RenderUtil.drawRect(ctx, sx, sy, sw, 1, oc);
                RenderUtil.drawRect(ctx, sx, sy + sw - 1, sw, 1, oc);
                RenderUtil.drawRect(ctx, sx, sy, 1, sw, oc);
                RenderUtil.drawRect(ctx, sx + sw - 1, sy, 1, sw, oc);
            }
        }
    }

    /** Returns the preset strip swatch index under the mouse, or -1. */
    private int presetStripAt(double mx, double my) {
        if (!presetStripOpen || stripY < 0) return -1;
        int sw = 12, gap = 3;
        int n = PRESET_NAMES.length;
        int w = n * (sw + gap) - gap;
        int x = Math.min(accentX - w + 12, width - w - 4);
        int y = stripY;
        for (int i = 0; i < n; i++) {
            int sx = x + 3 + i * (sw + gap);
            if (mx >= sx && mx <= sx + sw && my >= y + 3 && my <= y + 3 + sw) return i;
        }
        return -1;
    }

    private void drawSidebar(DrawContext ctx, int mx, int my) {
        int x = sidebarX(), y = sidebarY();
        int w = sidebarW();
        int rowH = Math.round(18 * guiScale());
        int h = Module.Category.values().length * rowH + HEADER_H + 2;

        RenderUtil.drawRoundedRect(ctx, x + 2, y + 2, w, h, 4f, RenderUtil.withAlpha(0x000000, 0.4f));
        RenderUtil.drawRoundedRect(ctx, x, y, w, h, 4f, VapeTheme.BG_PANEL);
        RenderUtil.drawRect(ctx, x, y, 2, h, RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.8f));

        ctx.drawTextWithShadow(textRenderer, "§7MODULES", x + 6, y + 4, VapeTheme.TEXT_DIM());

        int ry = y + HEADER_H;
        for (Module.Category cat : Module.Category.values()) {
            boolean hover = mx >= x && mx < x + w && my >= ry && my < ry + rowH;
            boolean onScreen = drawOrder.contains(cat) && !panelCollapsed(cat);

            float sel = sidebarSel.getOrDefault(cat, onScreen ? 0.8f : 0f);
            float target = onScreen ? 0.8f : (hover ? 0.45f : 0f);
            sel = sel + (target - sel) * 0.25f;
            sidebarSel.put(cat, sel);

            if (hover) {
                RenderUtil.drawRect(ctx, x + 2, ry, w - 4, rowH - 1,
                        RenderUtil.withAlpha(VapeTheme.BG_MODULE, 0.9f));
            }
            if (sel > 0.02f) {
                RenderUtil.drawRect(ctx, x + 2, ry + 2, 2, rowH - 5,
                        RenderUtil.withAlpha(VapeTheme.ACCENT(), sel));
            }

            int enabled = 0;
            if (JayHackClient.moduleManager != null) {
                for (Module m : JayHackClient.moduleManager.getModules()) {
                    if (m.getCategory() == cat && m.isEnabled()) enabled++;
                }
            }
            int nameCol = hover ? VapeTheme.TEXT() : (onScreen ? VapeTheme.ACCENT() : VapeTheme.TEXT_DIM());
            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 8, ry + 5, nameCol);
            String cnt = String.valueOf(enabled);
            ctx.drawTextWithShadow(textRenderer, cnt, x + w - textRenderer.getWidth(cnt) - 6, ry + 5,
                    enabled > 0 ? VapeTheme.ACCENT() : VapeTheme.TEXT_DIM());
            ry += rowH;
        }
    }

    private void drawPanel(DrawContext ctx, Module.Category cat, int mx, int my) {
        int x = panelX(cat), y = panelY(cat);
        int w = panelW();
        int rh = rowH();
        int sh = setH();
        boolean fold = panelCollapsed(cat);
        List<Module> list = modulesIn(cat);
        int h = panelHeight(cat);
        boolean topHover = cat == topCategoryAt(mx, my);

        // drop shadow + panel
        RenderUtil.drawRoundedRect(ctx, x + 2, y + 2, w, h, 4f, RenderUtil.withAlpha(0x000000, 0.45f));
        RenderUtil.drawRoundedRect(ctx, x, y, w, h, 4f, VapeTheme.BG_PANEL);
        RenderUtil.drawRect(ctx, x, y, 2, h, RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.8f));

        // header (accent underline like the reference)
        RenderUtil.drawRect(ctx, x + 2, y, w - 2, HEADER_H, VapeTheme.BG_HEADER);
        RenderUtil.drawRect(ctx, x + 2, y + HEADER_H - 1, w - 2, 1, RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.9f));
        String mark = fold ? "▸ " : "▾ ";
        ctx.drawTextWithShadow(textRenderer, mark + cat.displayName, x + 7, y + 5, VapeTheme.TEXT());
        String cnt = list.size() + "/" + enabledCount(cat);
        ctx.drawTextWithShadow(textRenderer, cnt, x + w - textRenderer.getWidth(cnt) - 6, y + 5, VapeTheme.TEXT_DIM());

        // hover outline (Respect-style bright border on the focused window)
        if (topHover) {
            int oc = RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.35f);
            RenderUtil.drawRect(ctx, x, y, w, 1, oc);
            RenderUtil.drawRect(ctx, x, y + h - 1, w, 1, oc);
            RenderUtil.drawRect(ctx, x, y, 1, h, oc);
            RenderUtil.drawRect(ctx, x + w - 1, y, 1, h, oc);
        }

        if (fold) return;

        int scrl = scroll.getOrDefault(cat, 0);
        int maxScroll = Math.max(0, list.size() - MAX_ROWS);
        scrl = MathHelper.clamp(scrl, 0, maxScroll);
        scroll.put(cat, scrl);

        int ry = y + HEADER_H + 2;
        int drawn = 0;
        int index = 0;
        for (Module m : list) {
            if (index < scrl) { index++; continue; }
            if (drawn >= MAX_ROWS) break;

            boolean hover = mx >= x && mx < x + w && my >= ry && my < ry + rh;
            drawModuleRow(ctx, m, x, ry, w, rh, hover, mx, my);
            ry += rh;
            drawn++;

            if (isExpanded(m) && !m.getSettings().isEmpty()) {
                float ep = expandProgress(m);
                if (ep > 0.02f) {
                    int sTotalH = Math.round(settingRows(m) * sh * ep);
                    RenderUtil.drawRect(ctx, x + 2, ry, w - 2, sTotalH, RenderUtil.withAlpha(VapeTheme.BG_SUNKEN, 0.92f * ep));
                    int sy = ry;
                    for (Setting s : m.getSettings()) {
                        if (sy >= ry + sTotalH) break;
                        int rows = (s instanceof NumberSetting) ? 2 : 1;
                        drawSetting(ctx, m, s, x + 4, sy, w - 8, sh, mx, my, ep, rows);
                        sy += rows * sh;
                    }
                }
                ry += Math.round(settingRows(m) * sh * ep);
            }
            index++;
        }

        // scroll indicator
        if (maxScroll > 0) {
            float pct = scrl / (float) maxScroll;
            int trackH = h - HEADER_H - 4;
            int thumbH = Math.max(8, trackH / 4);
            int thumbY = y + HEADER_H + 2 + (int) ((trackH - thumbH) * pct);
            RenderUtil.drawRect(ctx, x + w - 2, thumbY, 1, thumbH, RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.6f));
        }
    }

    private int enabledCount(Module.Category cat) {
        int n = 0;
        if (JayHackClient.moduleManager == null) return 0;
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getCategory() == cat && m.isEnabled()) n++;
        }
        return n;
    }

    private void drawModuleRow(DrawContext ctx, Module m, int x, int y, int w, int h,
                               boolean hover, int mx, int my) {
        boolean on = m.isEnabled();
        float hv = hoverRamp("row:" + m.getName(), hover);

        if (on) {
            RenderUtil.drawRect(ctx, x + 2, y, w - 2, h, RenderUtil.withAlpha(VapeTheme.BG_ENABLED, 0.9f));
        } else if (hv > 0.02f) {
            RenderUtil.drawRect(ctx, x + 2, y, w - 2, h, RenderUtil.withAlpha(VapeTheme.BG_MODULE, 0.95f * hv));
        }
        if (on) {
            RenderUtil.drawRect(ctx, x + 2, y + 1, 2, h - 2, VapeTheme.ACCENT());
        }

        // hold-to-bind progress strip
        if (m == holdModule) {
            float held = MathHelper.clamp((System.currentTimeMillis() - holdStart) / (float) HOLD_MS, 0f, 1f);
            RenderUtil.drawRect(ctx, x + 2, y + h - 2, (int) ((w - 4) * held), 2, VapeTheme.ACCENT());
        }

        String nameCol = on ? "§d" : (hv > 0.5f ? "§f" : "§7");
        String star = ClientSettings.isFavorite(m.getName()) ? "§6★ " : "";
        ctx.drawTextWithShadow(textRenderer, star + nameCol + m.getName(), x + 8, y + (h - 8) / 2, VapeTheme.TEXT());

        // right side: keybind chip when bound, settings arrow otherwise
        String key = safeKeyLabel(m);
        boolean hasSettings = !m.getSettings().isEmpty();
        if (!key.isEmpty()) {
            int kw = textRenderer.getWidth(key) + 6;
            int kx = x + w - kw - 6;
            RenderUtil.drawRect(ctx, kx, y + 2, kw, h - 4, RenderUtil.withAlpha(VapeTheme.BG_HEADER, 0.8f));
            ctx.drawTextWithShadow(textRenderer, key, kx + 3, y + (h - 8) / 2,
                    on ? VapeTheme.ACCENT_TEXT() : VapeTheme.TEXT_DIM());
        } else if (hasSettings) {
            String arrow = isExpanded(m) ? "▾" : "▸";
            ctx.drawTextWithShadow(textRenderer, arrow, x + w - 12, y + (h - 8) / 2,
                    hv > 0.5f ? VapeTheme.TEXT() : VapeTheme.TEXT_DIM());
        }
    }

    private void drawSetting(DrawContext ctx, Module owner, Setting s, int x, int y, int w, int h,
                             int mx, int my, float ep, int rows) {
        if (s instanceof BoolSetting b) {
            boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
            if (hover) RenderUtil.drawRect(ctx, x, y, w, h, RenderUtil.withAlpha(VapeTheme.BG_MODULE, 0.7f * ep));
            ctx.drawTextWithShadow(textRenderer, s.getName(), x + 8, y + (h - 8) / 2,
                    b.get() ? VapeTheme.TEXT() : VapeTheme.TEXT_DIM());
            // animated mini switch
            int swW = 18, swH = 8;
            int swX = x + w - swW - 6, swY = y + (h - swH) / 2;
            float p = togglePos(s, b.get());
            RenderUtil.drawRect(ctx, swX, swY, swW, swH,
                    p > 0.5f ? RenderUtil.withAlpha(VapeTheme.ACCENT(), 0.35f + 0.25f * p) : VapeTheme.BG_HEADER);
            int knob = swX + (int) (p * (swW - swH));
            RenderUtil.drawRect(ctx, knob, swY, swH, swH, p > 0.5f ? VapeTheme.ACCENT() : VapeTheme.TEXT_DIM());
        } else if (s instanceof ModeSetting mode) {
            boolean hover = mx >= x && mx < x + w && my >= y && my < y + h;
            if (hover) RenderUtil.drawRect(ctx, x, y, w, h, RenderUtil.withAlpha(VapeTheme.BG_MODULE, 0.7f * ep));
            ctx.drawTextWithShadow(textRenderer, s.getName(), x + 8, y + (h - 8) / 2, VapeTheme.TEXT_DIM());
            String val = mode.get();
            ctx.drawTextWithShadow(textRenderer, val, x + w - textRenderer.getWidth(val) - 6, y + (h - 8) / 2,
                    VapeTheme.ACCENT_TEXT());
        } else if (s instanceof NumberSetting num) {
            // label row
            ctx.drawTextWithShadow(textRenderer, s.getName(), x + 8, y + 1, VapeTheme.TEXT_DIM());
            String val = s.getDisplayValue();
            ctx.drawTextWithShadow(textRenderer, val, x + w - textRenderer.getWidth(val) - 6, y + 1,
                    VapeTheme.ACCENT_TEXT());
            // slider track row
            int slY = y + h / 2 + 1;
            int slH = 4;
            int slW = w - 14;
            int slX = x + 7;
            RenderUtil.drawRect(ctx, slX, slY, slW, slH, VapeTheme.BG_HEADER);
            double pct = (num.get() - num.getMin()) / Math.max(1e-9, num.getMax() - num.getMin());
            pct = MathHelper.clamp(pct, 0.0, 1.0);
            int fill = (int) (slW * pct);
            RenderUtil.drawRect(ctx, slX, slY, fill, slH, VapeTheme.ACCENT());
            RenderUtil.drawRect(ctx, slX + fill - 1, slY - 1, 2, slH + 2, VapeTheme.ACCENT_TEXT());
        }
    }

    // ------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y();
        int button = click.button();

        // preset strip swatches
        int swatch = presetStripAt(mouseX, mouseY);
        if (swatch >= 0) {
            GuiColors.applyPreset(PRESET_NAMES[swatch]);
            presetStripOpen = false;
            saveQuiet();
            try { com.jay.hackclient.util.Notifications.push("Theme", "Accent: " + PRESET_NAMES[swatch]); }
            catch (Throwable ignored) {}
            return true;
        }

        // search box
        if (searchBoxW > 0 && mouseX >= searchBoxX && mouseX <= searchBoxX + searchBoxW
                && mouseY >= searchBoxY && mouseY <= searchBoxY + searchBoxH) {
            searchFocused = true;
            return true;
        }
        searchFocused = false;

        // accent swatch toggles the preset strip
        if (accentX > 0 && mouseX >= accentX && mouseX <= accentX + 12
                && mouseY >= searchBoxY && mouseY <= searchBoxY + searchBoxH) {
            presetStripOpen = !presetStripOpen;
            return true;
        }
        if (presetStripOpen && !presetStripHit(mouseX, mouseY)) presetStripOpen = false;

        // panels — topmost first
        Module.Category hit = topCategoryAt((int) mouseX, (int) mouseY);
        if (hit != null) {
            bringToFront(hit);
            int px = panelX(hit), py = panelY(hit), pw = panelW();
            int rh = rowH(), sh = setH();
            boolean fold = panelCollapsed(hit);

            // header drag / fold
            if (mouseY >= py && mouseY < py + HEADER_H) {
                if (button == 0) {
                    dragCat = hit; panelDragged = false;
                    dragOx = mouseX - px; dragOy = mouseY - py;
                    return true;
                }
                if (button == 1) {
                    setPanelCollapsed(hit, !fold);
                    return true;
                }
            }

            if (!fold) {
                List<Module> list = modulesIn(hit);
                int scrl = MathHelper.clamp(scroll.getOrDefault(hit, 0), 0, Math.max(0, list.size() - MAX_ROWS));
                scroll.put(hit, scrl);
                int ry = py + HEADER_H + 2;
                int index = 0, drawn = 0;
                for (Module m : list) {
                    if (index < scrl) { index++; continue; }
                    if (drawn >= MAX_ROWS) break;

                    if (mouseX >= px && mouseX < px + pw && mouseY >= ry && mouseY < ry + rh) {
                        if (button == 0) {
                            holdModule = m;
                            holdStart = System.currentTimeMillis();
                            return true;
                        }
                        if (button == 1) {
                            if (!m.getSettings().isEmpty()) expanded.put(m, !isExpanded(m));
                            return true;
                        }
                        if (button == 2) {
                            ClientSettings.toggleFavorite(m.getName());
                            saveQuiet();
                            return true;
                        }
                    }

                    ry += rh;
                    drawn++;

                    if (isExpanded(m) && !m.getSettings().isEmpty()) {
                        int sTotalH = Math.round(settingRows(m) * sh);
                        if (expandProgress(m) > 0.95f && mouseY >= ry && mouseY < ry + sTotalH) {
                            if (button == 0) {
                                Setting s = hitSetting(m, px + 4, ry, pw - 8, sh, (int) mouseX, (int) mouseY);
                                if (s != null) { handleSettingClick(s); return true; }
                            }
                        }
                        ry += sTotalH;
                    }
                    index++;
                }
            }
            return true; // clicks inside a focused panel never fall through
        }

        // sidebar: focus panel, right-click folds/unfolds
        int sbRowH = Math.round(18 * guiScale());
        int sbY = sidebarY() + HEADER_H;
        if (mouseX >= sidebarX() && mouseX < sidebarX() + sidebarW()
                && mouseY >= sbY && mouseY < sbY + sbRowH * Module.Category.values().length) {
            int idx = (int) ((mouseY - sbY) / sbRowH);
            Module.Category[] cats = Module.Category.values();
            if (idx >= 0 && idx < cats.length) {
                Module.Category c = cats[idx];
                if (button == 0) {
                    if (panelCollapsed(c)) setPanelCollapsed(c, false);
                    bringToFront(c);
                    return true;
                }
                if (button == 1) {
                    setPanelCollapsed(c, !panelCollapsed(c));
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    private boolean presetStripHit(double mx, double my) {
        return presetStripAt(mx, my) >= 0
                || (mx >= accentX && mx <= accentX + 12 && my >= searchBoxY && my <= stripY + 21);
    }

    private Setting hitSetting(Module m, int sx, int sy, int w, int h, int mx, int my) {
        int y = sy;
        for (Setting s : m.getSettings()) {
            int rows = (s instanceof NumberSetting) ? 2 : 1;
            if (my >= y && my < y + h * rows) return s;
            y += h * rows;
        }
        return null;
    }

    private void handleSettingClick(Setting s) {
        try {
            if (s instanceof BoolSetting b) {
                b.toggle();
                saveQuiet();
            } else if (s instanceof ModeSetting mode) {
                mode.cycle();
                saveQuiet();
            } else if (s instanceof NumberSetting num) {
                // first click arms slider dragging; movement updates value
                draggingSlider = num;
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            if (holdModule != null) {
                long held = System.currentTimeMillis() - holdStart;
                if (held >= HOLD_MS && !panelDragged) {
                    bindingMode = true;
                    bindingModule = holdModule;
                } else if (!panelDragged) {
                    holdModule.toggle();
                    saveQuiet();
                }
                holdModule = null;
            }
            if (dragCat != null) {
                if (panelDragged) {
                    GuiLayout.set(dragCat, (float) (click.x() - dragOx), (float) (click.y() - dragOy));
                    saveQuiet();
                }
                dragCat = null;
                panelDragged = false;
            }
            if (draggingSlider != null) {
                draggingSlider = null;
                saveQuiet();
            }
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (draggingSlider != null) {
            NumberSetting num = draggingSlider;
            // slider geometry must match drawSetting (panel-local track x+7 .. w-14)
            Module.Category cat = categoryOfPanelContaining(click.x(), click.y());
            if (cat == null) cat = drawOrder.isEmpty() ? null : drawOrder.get(drawOrder.size() - 1);
            if (cat == null) return true;
            int px = panelX(cat);
            int pw = panelW();
            double rel = (click.x() - (px + 7)) / (double) (pw - 8 - 14);
            rel = MathUtil.clamp(rel, 0.0, 1.0);
            double v = num.getMin() + rel * (num.getMax() - num.getMin());
            num.set(v); // set() snaps to step + clamps
            return true;
        }
        if (dragCat != null) {
            panelDragged = true;
            float[] pos = GuiLayout.get(dragCat);
            pos[0] = (float) (click.x() - dragOx);
            pos[1] = (float) (click.y() - dragOy);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    private Module.Category categoryOfPanelContaining(double x, double y) {
        for (int i = drawOrder.size() - 1; i >= 0; i--) {
            Module.Category c = drawOrder.get(i);
            if (insidePanel(c, x, y)) return c;
        }
        return null;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horiz, double vert) {
        Module.Category cat = categoryOfPanelContaining(mouseX, mouseY);
        if (cat != null && !panelCollapsed(cat)) {
            int max = Math.max(0, modulesIn(cat).size() - MAX_ROWS);
            int scrl = scroll.getOrDefault(cat, 0) - (int) Math.signum(vert);
            scroll.put(cat, MathUtil.clamp(scrl, 0, max));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horiz, vert);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (bindingMode && bindingModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) {
                bindingMode = false;
                bindingModule = null;
                return true;
            }
            bindingModule.setKeyBind(key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE ? -1 : key);
            bindHintUntil = System.currentTimeMillis() + 1600;
            bindHintModule = bindingModule;
            bindingMode = false;
            bindingModule = null;
            saveQuiet();
            return true;
        }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                return true;
            }
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused) {
            char c = input.codepoint() > 0 ? (char) input.codepoint() : 0;
            if (c >= 32 && c < 127 && search.length() < 32) {
                search += c;
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
