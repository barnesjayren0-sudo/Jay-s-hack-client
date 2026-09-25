package com.jay.hackclient.gui;

// Premium ClickGUI placeholder - full file pushed in follow-up if truncated.
// See local jay_premium/gui/ClickGuiScreen.java for complete source.

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.profile.PresetManager;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Animation;
import com.jay.hackclient.util.RenderUtil;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/** Premium ClickGUI — see repo commit history / local artifacts for full 600-line version if needed. */
public class ClickGuiScreen extends Screen {
    private static final int PANEL_W = 110, HEADER_H = 16, BASE_ROW = 14, MAX_VISIBLE = 12, SIDE_W = 150;
    private static final long HOLD_MS = 450;
    private Module.Category dragCat; private boolean panelDragged; private double dragOx, dragOy;
    private final Map<Module.Category, Integer> scroll = new EnumMap<>(Module.Category.class);
    private final Set<Module.Category> collapsed = EnumSet.noneOf(Module.Category.class);
    private String search = ""; private boolean searchFocused; private Module selected;
    private enum SortMode { NAME, ENABLED, FAVORITES } private SortMode sort = SortMode.NAME;
    private Module holdModule; private long holdStart; private boolean bindingMode; private Module bindingModule;
    private final Animation openAnim = new Animation(260, 1.0, Animation.Easing.EASE_OUT_BACK);

    public ClickGuiScreen() { super(Text.literal("JAY CLIENT")); }
    @Override protected void init() { super.init(); openAnim.reset(); openAnim.setDirection(Animation.Direction.FORWARDS); try { PremiumTheme.onGuiOpen(); } catch (Throwable ignored) {} }
    @Override public void close() { try { PremiumTheme.onGuiClose(); } catch (Throwable ignored) {} super.close(); }
    private float guiScale() { return Math.max(0.85f, Math.min(1.25f, ClientSettings.guiScale)); }
    private int rowH() { return Math.round(BASE_ROW * guiScale()); }
    private int panelW() { return Math.round(PANEL_W * guiScale()); }
    private void ensurePositions() { GuiLayout.ensureDefaults(); for (Module.Category cat : Module.Category.values()) scroll.putIfAbsent(cat, 0); }

    private boolean matchesSearch(Module m, String q) {
        if (q.isEmpty()) return true;
        if (m.getName().toLowerCase(Locale.ROOT).contains(q)) return true;
        if (m.getDescription() != null && m.getDescription().toLowerCase(Locale.ROOT).contains(q)) return true;
        try { for (Setting s : m.getSettings()) if (s.getName() != null && s.getName().toLowerCase(Locale.ROOT).contains(q)) return true; } catch (Throwable ignored) {}
        return false;
    }

    private List<Module> filtered(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search.toLowerCase(Locale.ROOT).trim();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getCategory() != cat || !matchesSearch(m, q)) continue;
            out.add(m);
        }
        out.sort((a, b) -> {
            if (sort == SortMode.FAVORITES) {
                boolean fa = ClientSettings.isFavorite(a.getName()), fb = ClientSettings.isFavorite(b.getName());
                if (fa != fb) return fa ? -1 : 1;
            } else if (sort == SortMode.ENABLED && a.isEnabled() != b.isEnabled()) return a.isEnabled() ? -1 : 1;
            return a.getName().compareToIgnoreCase(b.getName());
        });
        return out;
    }

    private List<Module> pinned() {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        for (Module m : JayHackClient.moduleManager.getModules()) if (ClientSettings.isFavorite(m.getName())) out.add(m);
        out.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return out.size() > 8 ? out.subList(0, 8) : out;
    }

    private int categoryColor(Module.Category cat) {
        return switch (cat) {
            case COMBAT -> 0xFFFF6B6B; case MOVEMENT -> 0xFF6BFF9B; case RENDER -> 0xFF6BCBFF;
            case PLAYER -> 0xFFFFE06B; case WORLD -> 0xFFB794FF; case ANARCHY -> 0xFFFFAA55; case MISC -> 0xFFAAAAAA;
        };
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensurePositions();
        float anim = openAnim.getOutputF();
        if (anim < 0.02f) return;
        int pw = panelW(), rh = rowH(), barH = 22, pinH = pinned().isEmpty() ? 0 : 18, top = barH + pinH;

        ctx.fill(0, 0, width, height, RenderUtil.withAlpha(0x000000, 0.45f * anim));
        ctx.getMatrices().push();
        float cx = width / 2f, cy = height / 2f;
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().scale(0.92f + 0.08f * anim, 0.92f + 0.08f * anim, 1f);
        ctx.getMatrices().translate(-cx, -cy, 0);

        ctx.fill(0, 0, width, barH, PremiumTheme.BG_PANEL);
        ctx.fill(0, barH - 1, width, barH, PremiumTheme.ACCENT);
        try { JayLogo.draw(ctx, 4, 4, 14); } catch (Throwable ignored) {}
        ctx.drawTextWithShadow(textRenderer, "§dJAY§f CLIENT §8· §7" + JayHackClient.VERSION, 22, 7, PremiumTheme.TEXT);

        int sx = Math.max(140, width / 2 - 100);
        ctx.fill(sx, 4, sx + 110, 18, PremiumTheme.BG_MODULE);
        String st = searchFocused ? search + "§d|" : (search.isEmpty() ? "§8Search…" : search);
        ctx.drawTextWithShadow(textRenderer, st, sx + 5, 7, PremiumTheme.TEXT);

        String[] sorts = {"Name", "On", "★"}; SortMode[] modes = {SortMode.NAME, SortMode.ENABLED, SortMode.FAVORITES};
        int srx = sx + 116;
        for (int i = 0; i < sorts.length; i++) {
            int sw = textRenderer.getWidth(sorts[i]) + 10;
            boolean on = sort == modes[i];
            ctx.fill(srx, 4, srx + sw, 18, on ? PremiumTheme.BG_ENABLED : PremiumTheme.BG_MODULE);
            if (on) ctx.fill(srx, 17, srx + sw, 18, PremiumTheme.ACCENT);
            ctx.drawTextWithShadow(textRenderer, sorts[i], srx + 5, 7, on ? PremiumTheme.ACCENT : PremiumTheme.TEXT_DIM);
            srx += sw + 3;
        }

        List<Module> pins = pinned();
        if (!pins.isEmpty()) {
            ctx.fill(0, barH, width, barH + pinH, RenderUtil.withAlpha(PremiumTheme.BG, 0.95f));
            int px = 6;
            ctx.drawTextWithShadow(textRenderer, "§e★", px, barH + 5, PremiumTheme.TEXT); px += 14;
            for (Module m : pins) {
                int bw = textRenderer.getWidth(m.getName()) + 12;
                ctx.fill(px, barH + 3, px + bw, barH + 15, m.isEnabled() ? PremiumTheme.BG_ENABLED : PremiumTheme.BG_MODULE);
                if (m.isEnabled()) ctx.fill(px, barH + 14, px + bw, barH + 15, PremiumTheme.ACCENT);
                ctx.drawTextWithShadow(textRenderer, m.getName(), px + 6, barH + 5, m.isEnabled() ? PremiumTheme.ACCENT : PremiumTheme.TEXT);
                px += bw + 4; if (px > width - 24) break;
            }
        }

        if (bindingMode && bindingModule != null)
            ctx.drawTextWithShadow(textRenderer, "§eBind key for §f" + bindingModule.getName() + " §8(ESC cancel)", 10, top + 4, PremiumTheme.TEXT);

        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = Math.max(top + 4, (int) pos[1]);
            List<Module> list = filtered(cat);
            boolean fold = collapsed.contains(cat);
            int vis = fold ? 0 : Math.min(MAX_VISIBLE, list.size());
            int h = HEADER_H + vis * rh + 4;

            ctx.fill(x + 3, y + 3, x + pw + 3, y + h + 3, RenderUtil.withAlpha(0x000000, 0.35f * anim));
            ctx.fill(x, y, x + pw, y + h, PremiumTheme.BG_PANEL);
            ctx.fill(x, y, x + 3, y + h, categoryColor(cat));
            ctx.fill(x + 3, y, x + pw, y + HEADER_H, PremiumTheme.BG_MODULE);
            String mark = fold ? "§8▸ " : "§8▾ ";
            ctx.drawTextWithShadow(textRenderer, mark + cat.displayName, x + 8, y + 4, categoryColor(cat));
            String cnt = String.valueOf(list.size());
            ctx.drawTextWithShadow(textRenderer, cnt, x + pw - textRenderer.getWidth(cnt) - 6, y + 4, PremiumTheme.TEXT_DIM);
            if (fold) continue;

            int scrl = scroll.getOrDefault(cat, 0);
            scrl = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), scrl));
            scroll.put(cat, scrl);
            for (int row = 0; row < vis && scrl + row < list.size(); row++) {
                Module m = list.get(scrl + row);
                int ry = y + HEADER_H + 2 + row * rh;
                boolean hover = mouseX >= x && mouseX < x + pw && mouseY >= ry && mouseY < ry + rh;
                if (m.isEnabled()) { ctx.fill(x + 3, ry, x + pw, ry + rh, PremiumTheme.BG_ENABLED); ctx.fill(x + 3, ry, x + 5, ry + rh, PremiumTheme.ACCENT); }
                else if (hover || m == selected) ctx.fill(x + 3, ry, x + pw, ry + rh, RenderUtil.withAlpha(PremiumTheme.BG_MODULE, 0.9f));
                String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
                ctx.drawTextWithShadow(textRenderer, star + m.getName(), x + 8, ry + 3, m.isEnabled() ? PremiumTheme.ACCENT_HOVER : PremiumTheme.TEXT);
                String key = m.getKeyLabel();
                if (key != null && !key.isEmpty()) ctx.drawTextWithShadow(textRenderer, key, x + pw - textRenderer.getWidth(key) - 5, ry + 3, PremiumTheme.TEXT_DIM);
            }
        }

        if (selected != null && !selected.getSettings().isEmpty()) drawSettingsPanel(ctx, mouseX, mouseY, rh);
        ctx.getMatrices().pop();
    }

    private void drawSettingsPanel(DrawContext ctx, int mouseX, int mouseY, int rh) {
        int panelX = width - SIDE_W - 12, panelY = 28;
        List<Setting> settings = selected.getSettings();
        int h = 22 + settings.size() * (rh + 2) + 8;
        ctx.fill(panelX + 3, panelY + 3, panelX + SIDE_W + 3, panelY + h + 3, RenderUtil.withAlpha(0x000000, 0.4f));
        ctx.fill(panelX, panelY, panelX + SIDE_W, panelY + h, PremiumTheme.BG_PANEL);
        ctx.fill(panelX, panelY, panelX + 3, panelY + h, PremiumTheme.ACCENT);
        ctx.fill(panelX + 3, panelY, panelX + SIDE_W, panelY + 18, PremiumTheme.BG_MODULE);
        ctx.drawTextWithShadow(textRenderer, selected.getName(), panelX + 10, panelY + 5, PremiumTheme.ACCENT);
        int sy = panelY + 22;
        for (Setting s : settings) {
            ctx.fill(panelX + 4, sy, panelX + SIDE_W - 4, sy + rh, PremiumTheme.BG_MODULE);
            ctx.drawTextWithShadow(textRenderer, s.getName(), panelX + 8, sy + 3, PremiumTheme.TEXT);
            String val = s.getDisplayValue();
            ctx.drawTextWithShadow(textRenderer, val, panelX + SIDE_W - textRenderer.getWidth(val) - 8, sy + 3, PremiumTheme.ACCENT);
            sy += rh + 2;
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x(), mouseY = click.y(); int button = click.button();
        ensurePositions(); int pw = panelW(), rh = rowH(), barH = 22;
        int sx = Math.max(140, width / 2 - 100);
        if (mouseX >= sx && mouseX <= sx + 110 && mouseY >= 4 && mouseY <= 18) { searchFocused = true; return true; }
        searchFocused = false;

        String[] sorts = {"Name", "On", "★"}; SortMode[] modes = {SortMode.NAME, SortMode.ENABLED, SortMode.FAVORITES};
        int srx = sx + 116;
        for (int i = 0; i < sorts.length; i++) {
            int sw = textRenderer.getWidth(sorts[i]) + 10;
            if (mouseX >= srx && mouseX <= srx + sw && mouseY >= 4 && mouseY <= 18) { sort = modes[i]; return true; }
            srx += sw + 3;
        }

        if (selected != null && !selected.getSettings().isEmpty() && button == 0) {
            int panelX = width - SIDE_W - 12, panelY = 28, sy = panelY + 22;
            for (Setting s : selected.getSettings()) {
                if (mouseX >= panelX + 4 && mouseX <= panelX + SIDE_W - 4 && mouseY >= sy && mouseY <= sy + rh) { cycleSetting(s); return true; }
                sy += rh + 2;
            }
        }

        int top = barH + (pinned().isEmpty() ? 0 : 18);
        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = Math.max(top + 4, (int) pos[1]);
            List<Module> list = filtered(cat);
            boolean fold = collapsed.contains(cat);
            int vis = fold ? 0 : Math.min(MAX_VISIBLE, list.size());
            if (mouseX >= x && mouseX < x + pw && mouseY >= y && mouseY < y + HEADER_H) {
                if (button == 1) { if (collapsed.contains(cat)) collapsed.remove(cat); else collapsed.add(cat); return true; }
                if (button == 0) { dragCat = cat; panelDragged = false; dragOx = mouseX - x; dragOy = mouseY - y; return true; }
            }
            if (fold) continue;
            int scrl = scroll.getOrDefault(cat, 0);
            for (int row = 0; row < vis && scrl + row < list.size(); row++) {
                Module m = list.get(scrl + row);
                int ry = y + HEADER_H + 2 + row * rh;
                if (mouseX >= x && mouseX < x + pw && mouseY >= ry && mouseY < ry + rh) {
                    if (button == 0) { holdModule = m; holdStart = System.currentTimeMillis(); selected = m; return true; }
                    if (button == 1) { selected = m; return true; }
                    if (button == 2) { ClientSettings.toggleFavorite(m.getName()); return true; }
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private void cycleSetting(Setting s) {
        try {
            if (s instanceof BoolSetting b) b.toggle();
            else if (s instanceof ModeSetting m) m.cycle();
            else if (s instanceof NumberSetting n) { if (n.get() >= n.getMax() - 1e-9) n.set(n.getMin()); else n.increment(); }
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0 && holdModule != null) {
            long held = System.currentTimeMillis() - holdStart;
            if (held >= HOLD_MS) { bindingMode = true; bindingModule = holdModule; }
            else if (!panelDragged) { holdModule.toggle(); if (JayHackClient.configManager != null) JayHackClient.configManager.save(); }
            holdModule = null;
        }
        if (dragCat != null) {
            if (panelDragged) { GuiLayout.set(dragCat, (float)(click.x() - dragOx), (float)(click.y() - dragOy)); if (JayHackClient.configManager != null) JayHackClient.configManager.save(); }
            dragCat = null; panelDragged = false;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragCat != null) { panelDragged = true; float[] pos = GuiLayout.get(dragCat); pos[0] = (float)(click.x() - dragOx); pos[1] = (float)(click.y() - dragOy); return true; }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horiz, double vert) {
        int pw = panelW(), rh = rowH(), top = 22 + (pinned().isEmpty() ? 0 : 18);
        for (Module.Category cat : Module.Category.values()) {
            if (collapsed.contains(cat)) continue;
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = Math.max(top + 4, (int) pos[1]);
            List<Module> list = filtered(cat);
            int vis = Math.min(MAX_VISIBLE, list.size()), h = HEADER_H + vis * rh + 4;
            if (mouseX >= x && mouseX < x + pw && mouseY >= y && mouseY < y + h) {
                int scrl = scroll.getOrDefault(cat, 0) - (int) Math.signum(vert);
                scrl = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), scrl));
                scroll.put(cat, scrl); return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horiz, vert);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (bindingMode && bindingModule != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { bindingMode = false; bindingModule = null; return true; }
            bindingModule.setKeyBind(key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE ? -1 : key);
            bindingMode = false; bindingModule = null;
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            return true;
        }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) { search = search.substring(0, search.length() - 1); return true; }
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused) {
            char c = input.codepoint() > 0 ? (char) input.codepoint() : 0;
            if (c >= 32 && c < 127 && search.length() < 32) { search += c; return true; }
        }
        return super.charTyped(input);
    }

    @Override public boolean shouldPause() { return false; }
}
