package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.profile.PresetManager;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * ClickGUI — scale 0.85–1.25, ★ pin row, category collapse, hold-to-bind,
 * search matches module name/description/settings.
 */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 100;
    private static final int HEADER_H = 14;
    private static final int ROW_H = 13;
    private static final int MAX_VISIBLE = 10;
    private static final int SIDE_W = 120;
    private static final long HOLD_MS = 450;

    private Module.Category dragCat;
    private double dragOx, dragOy;
    private final Map<Module.Category, Integer> scroll = new EnumMap<>(Module.Category.class);
    private final Set<Module.Category> collapsed = EnumSet.noneOf(Module.Category.class);

    private String search = "";
    private boolean searchFocused;
    private long openMs = System.currentTimeMillis();
    private Module selected;
    private SortMode sort = SortMode.NAME;

    private Module holdModule;
    private long holdStart;
    private boolean bindingMode;
    private Module bindingModule;

    private enum SortMode { NAME, ENABLED, FAVORITES }

    public ClickGuiScreen() {
        super(Text.literal("JAY CLIENT"));
    }

    private float scale() {
        return Math.max(0.85f, Math.min(1.25f, ClientSettings.guiScale));
    }

    private void ensurePositions() {
        GuiLayout.ensureDefaults();
        for (Module.Category cat : Module.Category.values()) scroll.putIfAbsent(cat, 0);
    }

    private float openAnim() {
        float speed = ThemeEngine.animSpeed;
        float t = (System.currentTimeMillis() - openMs) / (180f / Math.max(0.4f, speed));
        if (t >= 1f) return 1f;
        return t * t * (3f - 2f * t);
    }

    private boolean matchesSearch(Module m, String q) {
        if (q.isEmpty()) return true;
        if (m.getName().toLowerCase(Locale.ROOT).contains(q)) return true;
        if (m.getDescription().toLowerCase(Locale.ROOT).contains(q)) return true;
        try {
            for (Setting<?> s : m.getSettings()) {
                if (s.getName() != null && s.getName().toLowerCase(Locale.ROOT).contains(q))
                    return true;
                if (s.getDescription() != null && s.getDescription().toLowerCase(Locale.ROOT).contains(q))
                    return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private List<Module> filtered(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search.toLowerCase(Locale.ROOT).trim();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getCategory() != cat) continue;
            if (!matchesSearch(m, q)) continue;
            out.add(m);
        }
        out.sort((a, b) -> {
            if (sort == SortMode.FAVORITES) {
                boolean fa = ClientSettings.isFavorite(a.getName());
                boolean fb = ClientSettings.isFavorite(b.getName());
                if (fa != fb) return fa ? -1 : 1;
            } else if (sort == SortMode.ENABLED) {
                if (a.isEnabled() != b.isEnabled()) return a.isEnabled() ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        return out;
    }

    private List<Module> pinned() {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (ClientSettings.isFavorite(m.getName())) out.add(m);
        }
        out.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        if (out.size() > 6) return out.subList(0, 6);
        return out;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensurePositions();
        float anim = openAnim();
        float sc = scale();

        ctx.fill(0, 0, width, height, GuiTheme.withAlpha(0x000000, (int) (0x50 * anim)));

        // Scale content from top-left for mobile readability
        ctx.getMatrices().pushMatrix();
        ctx.getMatrices().scale(sc, sc);

        int logicalW = (int) (width / sc);
        int logicalH = (int) (height / sc);
        int mx = (int) (mouseX / sc);
        int my = (int) (mouseY / sc);

        int barH = 18;
        int pinH = pinned().isEmpty() ? 0 : 16;
        int top = barH + pinH;

        ctx.fill(0, 0, logicalW, barH, GuiTheme.PANEL);
        try { JayLogo.draw(ctx, 3, 3, 12); } catch (Throwable ignored) {}
        ctx.drawTextWithShadow(textRenderer, "§bJAY§f · " + JayHackClient.VERSION, 18, 5, GuiTheme.TEXT);

        // Scale chip
        String scaleLabel = String.format(Locale.ROOT, "%.2fx", sc);
        int scaleW = textRenderer.getWidth(scaleLabel) + 16;
        int scaleX = logicalW - scaleW - 110;
        ctx.fill(scaleX, 3, scaleX + scaleW, 15, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, scaleLabel, scaleX + 4, 5, GuiTheme.ACCENT);

        int sx = Math.max(100, logicalW / 2 - 90);
        ctx.fill(sx, 3, sx + 90, 15, GuiTheme.PANEL2);
        String st = searchFocused ? search + "§7|" : (search.isEmpty() ? "§7search" : search);
        ctx.drawTextWithShadow(textRenderer, st, sx + 4, 5, GuiTheme.TEXT);

        String[] sorts = {"Name", "On", "★"};
        SortMode[] modes = {SortMode.NAME, SortMode.ENABLED, SortMode.FAVORITES};
        int srx = sx + 94;
        for (int i = 0; i < sorts.length; i++) {
            int sw = textRenderer.getWidth(sorts[i]) + 8;
            boolean on = sort == modes[i];
            ctx.fill(srx, 3, srx + sw, 15, on ? GuiTheme.ROW_ON : GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, sorts[i], srx + 4, 5, on ? GuiTheme.ACCENT : GuiTheme.TEXT_DIM);
            srx += sw + 2;
        }

        String[] tools = {"Prof", "Keys", "HUD", "Dbg", "Theme"};
        int tx = logicalW - 6;
        for (int i = tools.length - 1; i >= 0; i--) {
            int tw = textRenderer.getWidth(tools[i]) + 8;
            tx -= tw + 2;
            ctx.fill(tx, 3, tx + tw, 15, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, tools[i], tx + 4, 5, GuiTheme.TEXT_DIM);
        }

        // Favorites pin row
        List<Module> pins = pinned();
        if (!pins.isEmpty()) {
            ctx.fill(0, barH, logicalW, barH + pinH, GuiTheme.PANEL2);
            int px = 4;
            ctx.drawTextWithShadow(textRenderer, "§e★", px, barH + 4, GuiTheme.TEXT);
            px += 12;
            for (Module m : pins) {
                String label = m.getName();
                int pw = textRenderer.getWidth(label) + 10;
                ctx.fill(px, barH + 2, px + pw, barH + 14,
                        m.isEnabled() ? GuiTheme.ROW_ON : GuiTheme.BG);
                ctx.drawTextWithShadow(textRenderer, label, px + 5, barH + 4,
                        m.isEnabled() ? GuiTheme.ACCENT : GuiTheme.TEXT);
                px += pw + 3;
                if (px > logicalW - 20) break;
            }
        }

        if (bindingMode && bindingModule != null) {
            ctx.drawTextWithShadow(textRenderer,
                    "§ePress key for §f" + bindingModule.getName() + " §7(ESC cancel)",
                    8, top + 2, GuiTheme.TEXT);
        }

        if (logicalW > 400) {
            int px = 8;
            for (PresetManager.Preset p : PresetManager.Preset.values()) {
                int pw = textRenderer.getWidth(p.display) + 8;
                ctx.fill(px, top + 2, px + pw, top + 13, GuiTheme.PANEL2);
                ctx.drawTextWithShadow(textRenderer, p.display, px + 4, top + 4, GuiTheme.ACCENT);
                px += pw + 3;
            }
        }

        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0];
            int y = (int) pos[1];
            if (y < top) y = top + 2;
            List<Module> list = filtered(cat);
            boolean fold = collapsed.contains(cat);
            int vis = fold ? 0 : Math.min(MAX_VISIBLE, Math.max(0, list.size()));
            if (!fold && list.isEmpty()) vis = 0;
            int h = HEADER_H + vis * ROW_H + 2;

            ctx.fill(x + 2, y + 2, x + PANEL_W + 2, y + h + 2, GuiTheme.SHADOW);
            ctx.fill(x, y, x + PANEL_W, y + h, GuiTheme.BG);
            ctx.fill(x + 1, y + 1, x + PANEL_W - 1, y + HEADER_H, GuiTheme.PANEL);
            String foldMark = fold ? "§8+ " : "§8- ";
            ctx.drawTextWithShadow(textRenderer, foldMark + cat.displayName, x + 4, y + 3, categoryColor(cat));

            if (fold) continue;

            int scrl = scroll.getOrDefault(cat, 0);
            scrl = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), scrl));
            scroll.put(cat, scrl);

            for (int row = 0; row < vis && scrl + row < list.size(); row++) {
                Module m = list.get(scrl + row);
                int ry = y + HEADER_H + 1 + row * ROW_H;
                boolean hover = mx >= x && mx < x + PANEL_W && my >= ry && my < ry + ROW_H;
                if (m.isEnabled()) ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_ON);
                else if (hover || m == selected) ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_HOVER);
                String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
                ctx.drawTextWithShadow(textRenderer, star + m.getName(), x + 3, ry + 2,
                        m.isEnabled() ? GuiTheme.ACCENT : GuiTheme.TEXT);
            }
        }

        if (selected != null) {
            int spx = logicalW - SIDE_W - 6;
            int spy = top + 4;
            int sph = 100;
            ctx.fill(spx, spy, spx + SIDE_W, spy + sph, GuiTheme.BG);
            ctx.drawTextWithShadow(textRenderer, selected.getName(), spx + 4, spy + 4, GuiTheme.ACCENT);
            int dy = spy + 16;
            for (String line : wrap(selected.getDescription(), SIDE_W - 10)) {
                ctx.drawTextWithShadow(textRenderer, "§7" + line, spx + 4, dy, GuiTheme.TEXT_DIM);
                dy += 9;
                if (dy > spy + sph - 28) break;
            }
            ctx.fill(spx + 4, spy + sph - 18, spx + SIDE_W - 4, spy + sph - 4, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, "Settings", spx + 8, spy + sph - 14, GuiTheme.TEXT);
        }

        ctx.getMatrices().popMatrix();
    }

    private List<String> wrap(String s, int maxPx) {
        List<String> lines = new ArrayList<>();
        if (s == null || s.isEmpty()) return lines;
        StringBuilder cur = new StringBuilder();
        for (String word : s.split(" ")) {
            String trial = cur.isEmpty() ? word : cur + " " + word;
            if (textRenderer.getWidth(trial) > maxPx && !cur.isEmpty()) {
                lines.add(cur.toString());
                cur = new StringBuilder(word);
            } else cur = new StringBuilder(trial);
        }
        if (!cur.isEmpty()) lines.add(cur.toString());
        return lines;
    }

    private static int categoryColor(Module.Category cat) {
        return switch (cat) {
            case COMBAT -> 0xFFFF5555;
            case MOVEMENT -> 0xFF55FF55;
            case RENDER -> 0xFF55FFFF;
            case PLAYER -> 0xFFFFFF55;
            case WORLD -> 0xFFAA55FF;
            case ANARCHY -> 0xFFFFAA00;
            case MISC -> 0xFFAAAAAA;
        };
    }

    private double lx(double mx) { return mx / scale(); }
    private double ly(double my) { return my / scale(); }

    private Module.Category hitHeader(double mx, double my) {
        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = (int) pos[1];
            if (mx >= x && mx < x + PANEL_W && my >= y && my < y + HEADER_H) return cat;
        }
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = lx(click.x()), my = ly(click.y());
        int button = click.button();
        ensurePositions();
        float sc = scale();
        int logicalW = (int) (width / sc);
        int barH = 18;
        int pinH = pinned().isEmpty() ? 0 : 16;
        int top = barH + pinH;

        if (bindingMode) return true;

        // Scale adjust: left of scale label = decrease, right = increase
        if (button == 0 && my < barH) {
            String scaleLabel = String.format(Locale.ROOT, "%.2fx", sc);
            int scaleW = textRenderer.getWidth(scaleLabel) + 16;
            int scaleX = logicalW - scaleW - 110;
            if (mx >= scaleX && mx <= scaleX + scaleW) {
                float next = ClientSettings.guiScale + 0.05f;
                if (next > 1.25f) next = 0.85f;
                ClientSettings.guiScale = Math.round(next * 100f) / 100f;
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                return true;
            }

            int sx = Math.max(100, logicalW / 2 - 90);
            if (mx >= sx && mx <= sx + 90) { searchFocused = true; return true; }

            String[] sorts = {"Name", "On", "★"};
            SortMode[] modes = {SortMode.NAME, SortMode.ENABLED, SortMode.FAVORITES};
            int srx = sx + 94;
            for (int i = 0; i < sorts.length; i++) {
                int sw = textRenderer.getWidth(sorts[i]) + 8;
                if (mx >= srx && mx <= srx + sw) { sort = modes[i]; return true; }
                srx += sw + 2;
            }

            String[] tools = {"Prof", "Keys", "HUD", "Dbg", "Theme"};
            int tx = logicalW - 6;
            for (int i = tools.length - 1; i >= 0; i--) {
                int tw = textRenderer.getWidth(tools[i]) + 8;
                tx -= tw + 2;
                if (mx >= tx && mx <= tx + tw) {
                    switch (tools[i]) {
                        case "Prof" -> client.setScreen(new ProfileScreen(this));
                        case "Keys" -> client.setScreen(new KeybindScreen(this));
                        case "HUD" -> client.setScreen(new HudEditorScreen(this));
                        case "Dbg" -> client.setScreen(new DebugScreen(this));
                        case "Theme" -> {
                            ThemeEngine.cycle();
                            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                        }
                    }
                    return true;
                }
            }
            searchFocused = false;
        }

        // Pin row clicks
        if (button == 0 && pinH > 0 && my >= barH && my < barH + pinH) {
            int px = 16;
            for (Module m : pinned()) {
                int pw = textRenderer.getWidth(m.getName()) + 10;
                if (mx >= px && mx <= px + pw) {
                    m.toggle();
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    return true;
                }
                px += pw + 3;
            }
        }

        if (button == 0 && logicalW > 400 && my >= top + 2 && my <= top + 13) {
            int px = 8;
            for (PresetManager.Preset p : PresetManager.Preset.values()) {
                int pw = textRenderer.getWidth(p.display) + 8;
                if (mx >= px && mx <= px + pw) {
                    PresetManager.apply(p);
                    return true;
                }
                px += pw + 3;
            }
        }

        if (selected != null && button == 0) {
            int spx = logicalW - SIDE_W - 6;
            int spy = top + 4;
            int sph = 100;
            if (mx >= spx + 4 && mx <= spx + SIDE_W - 4 && my >= spy + sph - 18 && my <= spy + sph - 4) {
                client.setScreen(new SettingsScreen(this, selected));
                return true;
            }
        }

        Module.Category header = hitHeader(mx, my);
        if (header != null && button == 0) {
            // Shift-click or double-ish: collapse; else drag
            if (doubled) {
                if (collapsed.contains(header)) collapsed.remove(header);
                else collapsed.add(header);
                return true;
            }
            // Single click header toggles collapse when not dragging far
            dragCat = header;
            float[] pos = GuiLayout.get(header);
            dragOx = mx - pos[0];
            dragOy = my - pos[1];
            // Toggle collapse on short click — handled in mouseReleased if no drag
            return true;
        }

        for (Module.Category cat : Module.Category.values()) {
            if (collapsed.contains(cat)) continue;
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = (int) pos[1];
            List<Module> list = filtered(cat);
            int scrl = scroll.getOrDefault(cat, 0);
            int vis = Math.min(MAX_VISIBLE, Math.max(0, list.size()));
            for (int row = 0; row < vis && scrl + row < list.size(); row++) {
                Module m = list.get(scrl + row);
                int ry = y + HEADER_H + 1 + row * ROW_H;
                if (mx >= x && mx < x + PANEL_W && my >= ry && my < ry + ROW_H) {
                    if (button == 0) {
                        holdModule = m;
                        holdStart = System.currentTimeMillis();
                        selected = m;
                    } else if (button == 1) {
                        selected = m;
                        client.setScreen(new SettingsScreen(this, m));
                    } else if (button == 2) {
                        ClientSettings.toggleFavorite(m.getName());
                        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (dragCat != null) {
            double mx = lx(click.x()), my = ly(click.y());
            float nx = (float) Math.max(0, Math.min((width / scale()) - PANEL_W, mx - dragOx));
            float ny = (float) Math.max(34, Math.min((height / scale()) - 40, my - dragOy));
            GuiLayout.set(dragCat, nx, ny);
            // Cancel collapse toggle if dragged
            holdModule = null;
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (holdModule != null && click.button() == 0) {
            long held = System.currentTimeMillis() - holdStart;
            if (held >= HOLD_MS) {
                bindingMode = true;
                bindingModule = holdModule;
                Notifications.push("Bind", "Press a key for " + holdModule.getName());
            } else {
                holdModule.toggle();
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            }
            holdModule = null;
            return true;
        }
        if (dragCat != null) {
            // If almost no movement, treat as collapse toggle
            Module.Category c = dragCat;
            dragCat = null;
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            // Double-tap style: also allow middle path — single click collapses
            if (collapsed.contains(c)) collapsed.remove(c);
            else collapsed.add(c);
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        mx = lx(mx);
        my = ly(my);
        for (Module.Category cat : Module.Category.values()) {
            if (collapsed.contains(cat)) continue;
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = (int) pos[1];
            List<Module> list = filtered(cat);
            int body = HEADER_H + Math.min(MAX_VISIBLE, Math.max(1, list.size())) * ROW_H + 2;
            if (mx >= x && mx < x + PANEL_W && my >= y && my < y + body) {
                int scrl = scroll.getOrDefault(cat, 0) - (int) Math.signum(v);
                scrl = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), scrl));
                scroll.put(cat, scrl);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, h, v);
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
            if (key != GLFW.GLFW_KEY_UNKNOWN) {
                bindingModule.setKeyBind(key);
                Notifications.success("Bind", bindingModule.getName() + " bound");
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                bindingMode = false;
                bindingModule = null;
                return true;
            }
        }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { searchFocused = false; return true; }
            if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
                search = search.substring(0, search.length() - 1);
                return true;
            }
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) { client.setScreen(null); return true; }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused) {
            char c = (char) input.codepoint();
            if (c >= 32 && c < 127 && search.length() < 24) {
                search += c;
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
