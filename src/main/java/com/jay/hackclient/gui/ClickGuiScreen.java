package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.profile.PresetManager;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Compact ClickGUI — search, sort chips, favorites, side settings, tools. */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 100;
    private static final int HEADER_H = 14;
    private static final int ROW_H = 13;
    private static final int MAX_VISIBLE = 10;
    private static final int SIDE_W = 120;

    private Module.Category dragCat;
    private double dragOx, dragOy;
    private final Map<Module.Category, Integer> scroll = new EnumMap<>(Module.Category.class);

    private String search = "";
    private boolean searchFocused;
    private long openMs = System.currentTimeMillis();
    private Module selected;
    private SortMode sort = SortMode.NAME;

    private enum SortMode { NAME, CATEGORY, ENABLED, FAVORITES }

    public ClickGuiScreen() {
        super(Text.literal("JAY CLIENT"));
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

    private List<Module> filtered(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search.toLowerCase(Locale.ROOT).trim();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getCategory() != cat) continue;
            if (!q.isEmpty() && !m.getName().toLowerCase(Locale.ROOT).contains(q)
                    && !m.getDescription().toLowerCase(Locale.ROOT).contains(q)) continue;
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

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensurePositions();
        float anim = openAnim();
        ctx.fill(0, 0, width, height, GuiTheme.withAlpha(0x000000, (int) (0x50 * anim)));

        int barH = 18;
        ctx.fill(0, 0, width, barH, GuiTheme.PANEL);
        try { JayLogo.draw(ctx, 3, 3, 12); } catch (Throwable ignored) {}
        ctx.drawTextWithShadow(textRenderer, "§bJAY§f · " + JayHackClient.VERSION, 18, 5, GuiTheme.TEXT);

        int sx = Math.max(100, width / 2 - 90);
        ctx.fill(sx, 3, sx + 90, 15, GuiTheme.PANEL2);
        String st = searchFocused ? search + "§7|" : (search.isEmpty() ? "§7search" : search);
        ctx.drawTextWithShadow(textRenderer, st, sx + 4, 5, GuiTheme.TEXT);

        // Sort chips: Name / On / ★
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
        int tx = width - 6;
        for (int i = tools.length - 1; i >= 0; i--) {
            int tw = textRenderer.getWidth(tools[i]) + 8;
            tx -= tw + 2;
            ctx.fill(tx, 3, tx + tw, 15, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, tools[i], tx + 4, 5, GuiTheme.TEXT_DIM);
        }

        if (width > 400) {
            int px = 8;
            for (PresetManager.Preset p : PresetManager.Preset.values()) {
                int pw = textRenderer.getWidth(p.display) + 8;
                ctx.fill(px, barH + 2, px + pw, barH + 13, GuiTheme.PANEL2);
                ctx.drawTextWithShadow(textRenderer, p.display, px + 4, barH + 4, GuiTheme.ACCENT);
                px += pw + 3;
            }
        }

        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0];
            int y = (int) pos[1];
            List<Module> list = filtered(cat);
            int vis = Math.min(MAX_VISIBLE, Math.max(1, list.size()));
            int h = HEADER_H + vis * ROW_H + 2;
            ctx.fill(x + 2, y + 2, x + PANEL_W + 2, y + h + 2, GuiTheme.SHADOW);
            ctx.fill(x, y, x + PANEL_W, y + h, GuiTheme.BG);
            ctx.fill(x + 1, y + 1, x + PANEL_W - 1, y + HEADER_H, GuiTheme.PANEL);
            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 4, y + 3, categoryColor(cat));

            int sc = scroll.getOrDefault(cat, 0);
            sc = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), sc));
            scroll.put(cat, sc);

            for (int row = 0; row < vis && sc + row < list.size(); row++) {
                Module m = list.get(sc + row);
                int ry = y + HEADER_H + 1 + row * ROW_H;
                boolean hover = mouseX >= x && mouseX < x + PANEL_W && mouseY >= ry && mouseY < ry + ROW_H;
                if (m.isEnabled()) ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_ON);
                else if (hover || m == selected) ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_HOVER);
                String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
                ctx.drawTextWithShadow(textRenderer, star + m.getName(), x + 3, ry + 2,
                        m.isEnabled() ? GuiTheme.ACCENT : GuiTheme.TEXT);
            }
        }

        if (selected != null) {
            int spx = width - SIDE_W - 6;
            int spy = barH + 4;
            int sph = 90;
            ctx.fill(spx, spy, spx + SIDE_W, spy + sph, GuiTheme.BG);
            ctx.drawTextWithShadow(textRenderer, selected.getName(), spx + 4, spy + 4, GuiTheme.ACCENT);
            String desc = selected.getDescription();
            int dy = spy + 16;
            for (String line : wrap(desc, SIDE_W - 10)) {
                ctx.drawTextWithShadow(textRenderer, "§7" + line, spx + 4, dy, GuiTheme.TEXT_DIM);
                dy += 9;
                if (dy > spy + sph - 24) break;
            }
            ctx.fill(spx + 4, spy + sph - 18, spx + SIDE_W - 4, spy + sph - 4, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, "Settings", spx + 8, spy + sph - 14, GuiTheme.TEXT);
        }
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
        double mx = click.x(), my = click.y();
        int button = click.button();
        ensurePositions();
        int barH = 18;

        if (button == 0 && my < barH) {
            int sx = Math.max(100, width / 2 - 90);
            if (mx >= sx && mx <= sx + 90) { searchFocused = true; return true; }
            String[] sorts = {"Name", "On", "★"};
            SortMode[] modes = {SortMode.NAME, SortMode.ENABLED, SortMode.FAVORITES};
            int srx = sx + 94;
            for (int i = 0; i < sorts.length; i++) {
                int sw = textRenderer.getWidth(sorts[i]) + 8;
                if (mx >= srx && mx <= srx + sw) {
                    sort = modes[i];
                    return true;
                }
                srx += sw + 2;
            }
            String[] tools = {"Prof", "Keys", "HUD", "Dbg", "Theme"};
            int tx = width - 6;
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

        if (button == 0 && width > 400 && my >= barH + 2 && my <= barH + 13) {
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
            int spx = width - SIDE_W - 6;
            int spy = barH + 4;
            int sph = 90;
            if (mx >= spx + 4 && mx <= spx + SIDE_W - 4 && my >= spy + sph - 18 && my <= spy + sph - 4) {
                client.setScreen(new SettingsScreen(this, selected));
                return true;
            }
        }

        Module.Category header = hitHeader(mx, my);
        if (header != null && button == 0) {
            dragCat = header;
            float[] pos = GuiLayout.get(header);
            dragOx = mx - pos[0];
            dragOy = my - pos[1];
            return true;
        }

        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = (int) pos[1];
            List<Module> list = filtered(cat);
            int sc = scroll.getOrDefault(cat, 0);
            int vis = Math.min(MAX_VISIBLE, Math.max(1, list.size()));
            for (int row = 0; row < vis && sc + row < list.size(); row++) {
                Module m = list.get(sc + row);
                int ry = y + HEADER_H + 1 + row * ROW_H;
                if (mx >= x && mx < x + PANEL_W && my >= ry && my < ry + ROW_H) {
                    if (button == 0) {
                        m.toggle();
                        selected = m;
                        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
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
            float nx = (float) Math.max(0, Math.min(width - PANEL_W, click.x() - dragOx));
            float ny = (float) Math.max(18, Math.min(height - 40, click.y() - dragOy));
            GuiLayout.set(dragCat, nx, ny);
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragCat != null) {
            dragCat = null;
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0], y = (int) pos[1];
            List<Module> list = filtered(cat);
            int body = HEADER_H + Math.min(MAX_VISIBLE, Math.max(1, list.size())) * ROW_H + 2;
            if (mx >= x && mx < x + PANEL_W && my >= y && my < y + body) {
                int sc = scroll.getOrDefault(cat, 0) - (int) Math.signum(v);
                sc = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), sc));
                scroll.put(cat, sc);
                return true;
            }
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
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
