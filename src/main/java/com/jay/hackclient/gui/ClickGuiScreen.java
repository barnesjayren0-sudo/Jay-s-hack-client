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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Meteor-inspired floating multi-panel ClickGUI — small columns per category.
 */
public class ClickGuiScreen extends Screen {

    private static final int BG_PANEL = 0xF00C0C12;
    private static final int BG_HEADER = 0xF0141420;
    private static final int BG_ROW = 0xFF12121A;
    private static final int BG_ROW_ON = 0xFF152028;
    private static final int ACCENT = 0xFF3DDCFF;
    private static final int TEXT = 0xFFE8E8F0;
    private static final int TEXT_DIM = 0xFF707088;
    private static final int TOGGLE_OFF = 0xFF2A2A38;

    private static final String[] PROFILES = { "sword", "scout", "nethpot" };

    private static final Map<Module.Category, int[]> PANEL_POS = new HashMap<>();

    private String search = "";
    private boolean searchFocused;
    private Module.Category dragCat;
    private int dragOffX, dragOffY;

    private int panelW = 110;
    private int rowH = 14;
    private int headerH = 16;
    private boolean mobile;

    public ClickGuiScreen() {
        super(Text.literal("Jay"));
    }

    private void ensureLayout() {
        mobile = this.width < 480 || this.height < 360;
        panelW = mobile ? 96 : 112;
        rowH = mobile ? 13 : 14;
        headerH = mobile ? 15 : 16;

        if (PANEL_POS.isEmpty() && JayHackClient.moduleManager != null) {
            int x = 12;
            int y = 28;
            for (Module.Category c : Module.Category.values()) {
                if (JayHackClient.moduleManager.getByCategory(c).isEmpty()) continue;
                PANEL_POS.put(c, new int[]{x, y});
                x += panelW + 8;
                if (x + panelW > this.width - 8) {
                    x = 12;
                    y += 120;
                }
            }
        }
    }

    private List<Module> modulesOf(Module.Category cat) {
        List<Module> list = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return list;
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase(Locale.ROOT);
            for (Module m : JayHackClient.moduleManager.getByCategory(cat)) {
                if (m.getName().toLowerCase(Locale.ROOT).contains(q)) list.add(m);
            }
        } else {
            list.addAll(JayHackClient.moduleManager.getByCategory(cat));
        }
        return list;
    }

    private void fillRound(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 1, y1, x2 - 1, y2, color);
        ctx.fill(x1, y1 + 1, x2, y2 - 1, color);
    }

    private void drawToggle(DrawContext ctx, int x, int y, boolean on) {
        int tw = 16;
        int th = 8;
        ctx.fill(x, y, x + tw, y + th, on ? ACCENT : TOGGLE_OFF);
        int kn = 6;
        int kx = on ? x + tw - kn - 1 : x + 1;
        ctx.fill(kx, y + 1, kx + kn, y + th - 1, 0xFFFFFFFF);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensureLayout();

        // Dim world slightly — still see game (Meteor feel)
        ctx.fill(0, 0, this.width, this.height, 0x66000000);

        // Top bar: logo + search + profiles
        int barH = 22;
        ctx.fill(0, 0, this.width, barH, 0xF00A0A10);
        ctx.fill(0, barH - 1, this.width, barH, 0x553DDCFF);
        JayLogo.draw(ctx, 4, 3, 14);
        ctx.drawTextWithShadow(textRenderer, "§bJay", 22, 7, TEXT);

        // Search
        int sx = 50;
        int sw = Math.min(120, this.width / 4);
        ctx.fill(sx, 4, sx + sw, 18, 0xFF1A1A24);
        String sh = search.isEmpty() && !searchFocused ? "§8search" : "§f" + search + (searchFocused ? "§7|" : "");
        ctx.drawTextWithShadow(textRenderer, sh, sx + 4, 7, TEXT);

        // Profiles compact
        int px = this.width - 10;
        for (int i = PROFILES.length - 1; i >= 0; i--) {
            String p = PROFILES[i];
            int pw = textRenderer.getWidth(p) + 8;
            px -= pw + 3;
            boolean on = p.equalsIgnoreCase(ClientSettings.lastProfile);
            ctx.fill(px, 4, px + pw, 18, on ? 0xFF1A2A32 : 0xFF14141C);
            if (on) ctx.fill(px, 17, px + pw, 18, ACCENT);
            ctx.drawTextWithShadow(textRenderer, p, px + 4, 7, on ? ACCENT : TEXT_DIM);
        }

        if (JayHackClient.moduleManager == null) return;

        for (Module.Category cat : Module.Category.values()) {
            List<Module> mods = modulesOf(cat);
            if (mods.isEmpty() && (search == null || search.isBlank())) continue;
            if (mods.isEmpty()) continue;

            int[] pos = PANEL_POS.computeIfAbsent(cat, c -> new int[]{20, 40});
            int x = pos[0];
            int y = pos[1];
            int bodyH = Math.min(mods.size(), mobile ? 10 : 14) * rowH + 2;
            int totalH = headerH + bodyH;

            // Shadow
            ctx.fill(x + 2, y + 2, x + panelW + 2, y + totalH + 2, 0x44000000);
            // Panel
            fillRound(ctx, x, y, x + panelW, y + totalH, BG_PANEL);
            // Header
            ctx.fill(x, y, x + panelW, y + headerH, BG_HEADER);
            ctx.fill(x, y, x + panelW, y + 1, ACCENT);
            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 4, y + 4, ACCENT);

            int maxRows = mobile ? 10 : 14;
            int row = 0;
            for (Module m : mods) {
                if (row >= maxRows) break;
                int ry = y + headerH + row * rowH;
                boolean hover = mouseX >= x && mouseX < x + panelW && mouseY >= ry && mouseY < ry + rowH;
                if (m.isEnabled() || hover) {
                    ctx.fill(x + 1, ry, x + panelW - 1, ry + rowH, m.isEnabled() ? BG_ROW_ON : BG_ROW);
                }
                String name = m.getName();
                if (textRenderer.getWidth(name) > panelW - 28) {
                    while (textRenderer.getWidth(name + "..") > panelW - 28 && name.length() > 2) {
                        name = name.substring(0, name.length() - 1);
                    }
                    name = name + "..";
                }
                ctx.drawTextWithShadow(textRenderer, name, x + 4, ry + 3,
                        m.isEnabled() ? TEXT : TEXT_DIM);
                drawToggle(ctx, x + panelW - 20, ry + 3, m.isEnabled());
                row++;
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();
        ensureLayout();

        // Search focus
        int sx = 50;
        int sw = Math.min(120, this.width / 4);
        if (button == 0 && mouseY < 22 && mouseX >= sx && mouseX <= sx + sw) {
            searchFocused = true;
            return true;
        }

        // Profiles
        if (button == 0 && mouseY < 22) {
            int px = this.width - 10;
            for (int i = PROFILES.length - 1; i >= 0; i--) {
                String p = PROFILES[i];
                int pw = textRenderer.getWidth(p) + 8;
                px -= pw + 3;
                if (mouseX >= px && mouseX < px + pw) {
                    applyProfile(p);
                    return true;
                }
            }
        }

        if (JayHackClient.moduleManager == null) return super.mouseClicked(click, doubled);

        for (Module.Category cat : Module.Category.values()) {
            List<Module> mods = modulesOf(cat);
            if (mods.isEmpty()) continue;
            int[] pos = PANEL_POS.get(cat);
            if (pos == null) continue;
            int x = pos[0];
            int y = pos[1];
            int maxRows = mobile ? 10 : 14;
            int bodyH = Math.min(mods.size(), maxRows) * rowH + 2;

            // Drag header
            if (button == 0 && mouseX >= x && mouseX < x + panelW && mouseY >= y && mouseY < y + headerH) {
                dragCat = cat;
                dragOffX = (int) mouseX - x;
                dragOffY = (int) mouseY - y;
                searchFocused = false;
                return true;
            }

            // Module rows
            int row = 0;
            for (Module m : mods) {
                if (row >= maxRows) break;
                int ry = y + headerH + row * rowH;
                if (mouseX >= x && mouseX < x + panelW && mouseY >= ry && mouseY < ry + rowH) {
                    if (button == 1) {
                        if (client != null) client.setScreen(new SettingsScreen(this, m));
                        return true;
                    }
                    if (button == 0) {
                        if (JayHackClient.moduleManager.isFrozen()) {
                            if (client != null && client.player != null) {
                                client.player.sendMessage(Text.literal("§8[§bJay§8] §cUnpanic first"), false);
                            }
                        } else {
                            m.toggle();
                        }
                        return true;
                    }
                }
                row++;
            }
        }

        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragCat != null) {
            int[] pos = PANEL_POS.get(dragCat);
            if (pos != null) {
                pos[0] = (int) click.x() - dragOffX;
                pos[1] = (int) click.y() - dragOffY;
                pos[0] = Math.max(0, Math.min(this.width - panelW, pos[0]));
                pos[1] = Math.max(22, Math.min(this.height - 40, pos[1]));
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

    private void applyProfile(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "sword" -> LegitProfile.applySword();
            case "scout" -> LegitProfile.applyScout();
            case "nethpot" -> LegitProfile.applyNethpot();
            default -> { return; }
        }
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§8[§bJay§8] §a" + name), false);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        if (searchFocused && keyCode == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) {
            search = search.substring(0, search.length() - 1);
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
