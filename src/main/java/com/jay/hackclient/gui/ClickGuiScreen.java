package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.profile.LegitProfile;
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

/** Floating category panels — compact for mobile. */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 98;
    private static final int HEADER_H = 14;
    private static final int ROW_H = 11;
    private static final int MAX_VISIBLE = 10;

    private Module.Category dragCat;
    private double dragOx, dragOy;

    private final Map<Module.Category, Integer> scroll = new EnumMap<>(Module.Category.class);

    private String search = "";
    private boolean searchFocused;
    private long openMs = System.currentTimeMillis();

    public ClickGuiScreen() {
        super(Text.literal("JAY CLIENT"));
    }

    private void ensurePositions() {
        GuiLayout.ensureDefaults();
        for (Module.Category cat : Module.Category.values()) {
            scroll.putIfAbsent(cat, 0);
        }
    }

    private float openAnim() {
        float t = (System.currentTimeMillis() - openMs) / 180f;
        if (t >= 1f) return 1f;
        return t * t * (3f - 2f * t);
    }

    private void panelBg(DrawContext ctx, int x, int y, int w, int h) {
        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, GuiTheme.SHADOW);
        ctx.fill(x, y, x + w, y + h, GuiTheme.BG);
    }

    private List<Module> filtered(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search.toLowerCase(Locale.ROOT).trim();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getCategory() != cat) continue;
            if (!q.isEmpty() && !m.getName().toLowerCase(Locale.ROOT).contains(q)) continue;
            out.add(m);
        }
        return out;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensurePositions();
        float anim = openAnim();
        ctx.fill(0, 0, width, height, GuiTheme.withAlpha(0x000000, (int) (0x50 * anim)));

        int barH = 16;
        ctx.fill(0, 0, width, barH, GuiTheme.PANEL);
        try { JayLogo.draw(ctx, 3, 2, 12); } catch (Throwable ignored) {}
        ctx.drawTextWithShadow(textRenderer, "§bJAY§f · " + JayHackClient.VERSION, 18, 4, GuiTheme.TEXT);

        int sx = Math.max(90, width / 2 - 48);
        int sw = 96;
        ctx.fill(sx, 2, sx + sw, 14, GuiTheme.PANEL2);
        String st = searchFocused ? search + "§7|" : (search.isEmpty() ? "§7search" : search);
        ctx.drawTextWithShadow(textRenderer, st, sx + 3, 4, GuiTheme.TEXT);

        String[] chips = {"sword", "anarchy", "scout", "builder"};
        int cx = width - 6;
        for (int i = chips.length - 1; i >= 0; i--) {
            String c = chips[i];
            int pw = textRenderer.getWidth(c) + 6;
            cx -= pw + 2;
            boolean on = c.equalsIgnoreCase(ClientSettings.lastProfile);
            ctx.fill(cx, 2, cx + pw, 14, on ? GuiTheme.ROW_ON : GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, c, cx + 3, 4, on ? GuiTheme.ACCENT : GuiTheme.TEXT_DIM);
        }

        for (Module.Category cat : Module.Category.values()) {
            float[] pos = GuiLayout.get(cat);
            int x = (int) pos[0];
            int y = (int) pos[1];
            List<Module> list = filtered(cat);
            int vis = Math.min(MAX_VISIBLE, Math.max(1, list.size()));
            int bodyH = vis * ROW_H + 2;
            int h = HEADER_H + bodyH;
            panelBg(ctx, x, y, PANEL_W, h);
            ctx.fill(x + 1, y + 1, x + PANEL_W - 1, y + HEADER_H, GuiTheme.PANEL);
            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 3, y + 3, categoryColor(cat));
            String cstr = String.valueOf(list.size());
            ctx.drawTextWithShadow(textRenderer, cstr,
                    x + PANEL_W - 3 - textRenderer.getWidth(cstr), y + 3, GuiTheme.TEXT_DIM);

            int sc = scroll.getOrDefault(cat, 0);
            sc = Math.max(0, Math.min(Math.max(0, list.size() - MAX_VISIBLE), sc));
            scroll.put(cat, sc);

            for (int row = 0; row < vis && sc + row < list.size(); row++) {
                Module m = list.get(sc + row);
                int ry = y + HEADER_H + 1 + row * ROW_H;
                boolean hover = mouseX >= x && mouseX < x + PANEL_W && mouseY >= ry && mouseY < ry + ROW_H;
                if (m.isEnabled()) ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_ON);
                else if (hover) ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_HOVER);
                ctx.drawTextWithShadow(textRenderer, m.getName(), x + 3, ry + 1,
                        m.isEnabled() ? GuiTheme.ACCENT : GuiTheme.TEXT);
                if (m.getKeyBind() > 0) {
                    String key = m.getKeyLabel();
                    if (key.length() > 3) key = key.substring(0, 3);
                    ctx.drawTextWithShadow(textRenderer, key,
                            x + PANEL_W - 3 - textRenderer.getWidth(key), ry + 1, GuiTheme.TEXT_DIM);
                }
            }
        }
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

        if (button == 0 && my < 16) {
            int sx = Math.max(90, width / 2 - 48);
            if (mx >= sx && mx <= sx + 96) { searchFocused = true; return true; }
            String[] chips = {"sword", "anarchy", "scout", "builder"};
            int cx = width - 6;
            for (int i = chips.length - 1; i >= 0; i--) {
                String c = chips[i];
                int pw = textRenderer.getWidth(c) + 6;
                cx -= pw + 2;
                if (mx >= cx && mx <= cx + pw) {
                    switch (c) {
                        case "sword" -> LegitProfile.applySword();
                        case "anarchy" -> LegitProfile.applyAnarchy();
                        case "scout" -> LegitProfile.applyScout();
                        case "builder" -> LegitProfile.applyBuilder();
                        default -> {}
                    }
                    ClientSettings.lastProfile = c;
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    return true;
                }
            }
            searchFocused = false;
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
                        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    } else if (button == 1) {
                        client.setScreen(new SettingsScreen(this, m));
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
            float ny = (float) Math.max(16, Math.min(height - 40, click.y() - dragOy));
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
    public boolean shouldPause() {
        return false;
    }
}
