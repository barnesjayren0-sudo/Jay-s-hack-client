package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.profile.LegitProfile;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
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

/**
 * Floating category panels (Meteor-inspired) with premium dark glass styling.
 */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 118;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 14;
    private static final int MAX_VISIBLE = 12;

    private static final Map<Module.Category, float[]> POS = new EnumMap<>(Module.Category.class);
    private static boolean positioned;

    private Module.Category dragCat;
    private double dragOx, dragOy;

    private final Map<Module.Category, Integer> scroll = new EnumMap<>(Module.Category.class);
    private Module.Category settingsHover;

    private String search = "";
    private boolean searchFocused;
    private long openMs = System.currentTimeMillis();

    public ClickGuiScreen() {
        super(Text.literal("JAY CLIENT"));
    }

    private void ensurePositions() {
        if (positioned && !POS.isEmpty()) return;
        Module.Category[] cats = Module.Category.values();
        int gap = 8;
        int startX = 12;
        int startY = 28;
        for (int i = 0; i < cats.length; i++) {
            float x = startX + (i % 4) * (PANEL_W + gap);
            float y = startY + (i / 4) * 160;
            POS.put(cats[i], new float[]{x, y});
            scroll.putIfAbsent(cats[i], 0);
        }
        positioned = true;
    }

    private float openAnim() {
        float t = (System.currentTimeMillis() - openMs) / 160f;
        if (t >= 1f) return 1f;
        return 1f - (1f - t) * (1f - t);
    }

    private List<Module> mods(Module.Category cat) {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        for (Module m : JayHackClient.moduleManager.getByCategory(cat)) {
            if (q.isEmpty()
                    || m.getName().toLowerCase(Locale.ROOT).contains(q)
                    || m.getDescription().toLowerCase(Locale.ROOT).contains(q)) {
                out.add(m);
            }
        }
        return out;
    }

    private void panelBg(DrawContext ctx, int x, int y, int w, int h) {
        // soft shadow
        ctx.fill(x + 2, y + 3, x + w + 2, y + h + 3, 0x55000000);
        // glass body
        ctx.fill(x, y, x + w, y + h, GuiTheme.BG);
        // border
        ctx.fill(x, y, x + w, y + 1, GuiTheme.BORDER);
        ctx.fill(x, y + h - 1, x + w, y + h, GuiTheme.BORDER);
        ctx.fill(x, y, x + 1, y + h, GuiTheme.BORDER);
        ctx.fill(x + w - 1, y, x + w, y + h, GuiTheme.BORDER);
        // accent top
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, GuiTheme.ACCENT);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensurePositions();
        float anim = openAnim();

        // Light dim — keep world visible (Meteor vibe)
        ctx.fill(0, 0, width, height, GuiTheme.withAlpha(0x000000, (int) (0x55 * anim)));

        // Top bar: logo + search + profiles
        int barH = 22;
        ctx.fill(0, 0, width, barH, GuiTheme.PANEL);
        ctx.fill(0, barH, width, barH + 1, GuiTheme.BORDER);
        JayLogo.draw(ctx, 6, 3, 16);
        ctx.drawTextWithShadow(textRenderer, "§bJAY§f · " + JayHackClient.VERSION, 26, 7, GuiTheme.TEXT);

        // Search pill
        int sx = width / 2 - 70;
        int sw = 140;
        ctx.fill(sx, 3, sx + sw, 19, GuiTheme.PANEL2);
        if (searchFocused) ctx.fill(sx, 18, sx + sw, 19, GuiTheme.ACCENT);
        String ph = search.isEmpty() && !searchFocused ? "§8Search..." : "§f" + search + (searchFocused ? "§7|" : "");
        ctx.drawTextWithShadow(textRenderer, ph, sx + 6, 7, GuiTheme.TEXT);

        // Profile chips
        String[] profiles = {"anarchy", "sword", "scout", "nethpot"};
        int px = width - 8;
        for (int i = profiles.length - 1; i >= 0; i--) {
            String p = profiles[i];
            int pw = textRenderer.getWidth(p) + 10;
            px -= pw + 3;
            boolean on = p.equalsIgnoreCase(ClientSettings.lastProfile);
            ctx.fill(px, 4, px + pw, 18, on ? GuiTheme.ROW_ON : GuiTheme.PANEL2);
            if (on) ctx.fill(px, 17, px + pw, 18, GuiTheme.ACCENT);
            ctx.drawTextWithShadow(textRenderer, p, px + 5, 7, on ? GuiTheme.ACCENT : GuiTheme.TEXT_DIM);
        }

        // Floating panels
        int yOff = (int) ((1f - anim) * 8);
        for (Module.Category cat : Module.Category.values()) {
            List<Module> list = mods(cat);
            if (list.isEmpty() && !search.isBlank()) continue;

            float[] pos = POS.computeIfAbsent(cat, c -> new float[]{20, 40});
            int x = (int) pos[0];
            int y = (int) pos[1] + yOff;

            int visible = Math.min(MAX_VISIBLE, Math.max(1, list.size()));
            int bodyH = visible * ROW_H + 4;
            int h = HEADER_H + bodyH;

            panelBg(ctx, x, y, PANEL_W, h);

            // Header
            ctx.fill(x + 1, y + 2, x + PANEL_W - 1, y + HEADER_H, GuiTheme.PANEL);
            String title = cat.displayName;
            ctx.drawTextWithShadow(textRenderer, title, x + 6, y + 5, GuiTheme.ACCENT);
            int count = list.size();
            String cstr = String.valueOf(count);
            ctx.drawTextWithShadow(textRenderer, cstr,
                    x + PANEL_W - 6 - textRenderer.getWidth(cstr), y + 5, GuiTheme.TEXT_DIM);

            int sc = scroll.getOrDefault(cat, 0);
            int row = 0;
            for (int i = 0; i < list.size(); i++) {
                if (i < sc) continue;
                if (row >= MAX_VISIBLE) break;
                Module m = list.get(i);
                int ry = y + HEADER_H + 2 + row * ROW_H;
                boolean hover = mouseX >= x && mouseX < x + PANEL_W && mouseY >= ry && mouseY < ry + ROW_H;

                if (m.isEnabled()) {
                    ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_ON);
                    ctx.fill(x + 1, ry, x + 2, ry + ROW_H, GuiTheme.ACCENT);
                } else if (hover) {
                    ctx.fill(x + 1, ry, x + PANEL_W - 1, ry + ROW_H, GuiTheme.ROW_HOVER);
                }

                int nameColor = m.isEnabled() ? GuiTheme.TEXT : 0xFFB0B6C4;
                ctx.drawTextWithShadow(textRenderer, m.getName(), x + 6, ry + 3, nameColor);

                String key = m.getKeyLabel();
                if (!key.isEmpty()) {
                    ctx.drawTextWithShadow(textRenderer, key,
                            x + PANEL_W - 6 - textRenderer.getWidth(key), ry + 3, GuiTheme.TEXT_DIM);
                }
                row++;
            }
        }

        // Hint
        ctx.drawTextWithShadow(textRenderer, "§8LMB toggle · RMB settings · drag headers",
                8, height - 12, GuiTheme.TEXT_DIM);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private Module.Category headerAt(double mx, double my) {
        for (Module.Category cat : Module.Category.values()) {
            float[] pos = POS.get(cat);
            if (pos == null) continue;
            int x = (int) pos[0], y = (int) pos[1];
            if (mx >= x && mx < x + PANEL_W && my >= y && my < y + HEADER_H) return cat;
        }
        return null;
    }

    private Module moduleAt(double mx, double my) {
        for (Module.Category cat : Module.Category.values()) {
            List<Module> list = mods(cat);
            float[] pos = POS.get(cat);
            if (pos == null) continue;
            int x = (int) pos[0], y = (int) pos[1];
            int sc = scroll.getOrDefault(cat, 0);
            int row = 0;
            for (int i = 0; i < list.size(); i++) {
                if (i < sc) continue;
                if (row >= MAX_VISIBLE) break;
                int ry = y + HEADER_H + 2 + row * ROW_H;
                if (mx >= x && mx < x + PANEL_W && my >= ry && my < ry + ROW_H) {
                    return list.get(i);
                }
                row++;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();
        ensurePositions();

        // Search bar
        int sx = width / 2 - 70;
        if (button == 0 && mx >= sx && mx <= sx + 140 && my >= 3 && my <= 19) {
            searchFocused = true;
            return true;
        }

        // Profiles
        if (button == 0 && my >= 4 && my <= 18) {
            String[] profiles = {"anarchy", "sword", "scout", "nethpot"};
            int px = width - 8;
            for (int i = profiles.length - 1; i >= 0; i--) {
                String p = profiles[i];
                int pw = textRenderer.getWidth(p) + 10;
                px -= pw + 3;
                if (mx >= px && mx < px + pw) {
                    applyProfile(p);
                    return true;
                }
            }
        }

        // Drag header
        if (button == 0) {
            Module.Category head = headerAt(mx, my);
            if (head != null) {
                dragCat = head;
                float[] pos = POS.get(head);
                dragOx = mx - pos[0];
                dragOy = my - pos[1];
                searchFocused = false;
                return true;
            }
        }

        Module m = moduleAt(mx, my);
        if (m != null) {
            if (button == 1) {
                if (client != null) client.setScreen(new SettingsScreen(this, m));
                return true;
            }
            if (button == 0) {
                if (JayHackClient.moduleManager != null && JayHackClient.moduleManager.isFrozen()) {
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§8[§bJay§8] §cUnpanic first"), false);
                    }
                } else {
                    m.toggle();
                }
                return true;
            }
        }

        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragCat != null) {
            float[] pos = POS.get(dragCat);
            if (pos != null) {
                pos[0] = (float) (click.x() - dragOx);
                pos[1] = (float) (click.y() - dragOy);
                // clamp lightly
                pos[0] = Math.max(0, Math.min(width - PANEL_W, pos[0]));
                pos[1] = Math.max(24, Math.min(height - 40, pos[1]));
            }
            return true;
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        dragCat = null;
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        ensurePositions();
        for (Module.Category cat : Module.Category.values()) {
            float[] pos = POS.get(cat);
            if (pos == null) continue;
            int x = (int) pos[0], y = (int) pos[1];
            List<Module> list = mods(cat);
            int h = HEADER_H + Math.min(MAX_VISIBLE, Math.max(1, list.size())) * ROW_H + 4;
            if (mouseX >= x && mouseX < x + PANEL_W && mouseY >= y && mouseY < y + h) {
                int sc = scroll.getOrDefault(cat, 0);
                int max = Math.max(0, list.size() - MAX_VISIBLE);
                if (vAmount > 0) sc = Math.max(0, sc - 1);
                else if (vAmount < 0) sc = Math.min(max, sc + 1);
                scroll.put(cat, sc);
                return true;
            }
        }
        return true;
    }

    private void applyProfile(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "sword" -> LegitProfile.applySword();
            case "scout" -> LegitProfile.applyScout();
            case "nethpot" -> LegitProfile.applyNethpot();
            case "anarchy" -> LegitProfile.applyAnarchy();
            default -> { return; }
        }
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§8[§bJay§8] §aProfile §f" + name), false);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || (key == GLFW.GLFW_KEY_RIGHT_SHIFT && !searchFocused)) {
            close();
            return true;
        }
        if (searchFocused && key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
            return true;
        }
        if (searchFocused && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            searchFocused = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused) {
            int cp = input.codepoint();
            if (cp >= 32 && cp < 127 && search.length() < 24) {
                search += (char) cp;
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
