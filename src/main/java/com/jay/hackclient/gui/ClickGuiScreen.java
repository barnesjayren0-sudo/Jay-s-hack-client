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

/** Compact Meteor-style floating multi-panel ClickGUI. */
public class ClickGuiScreen extends Screen {

    private static final int BG_PANEL = 0xE80A0A10;
    private static final int BG_HEADER = 0xF0121220;
    private static final int BG_ROW = 0xFF101018;
    private static final int BG_ROW_ON = 0xFF142028;
    private static final int ACCENT = 0xFF3DDCFF;
    private static final int TEXT = 0xFFE8E8F0;
    private static final int TEXT_DIM = 0xFF686880;
    private static final int TOGGLE_OFF = 0xFF282838;

    private static final String[] PROFILES = { "sword", "scout", "nethpot" };
    private static final Map<Module.Category, int[]> PANEL_POS = new HashMap<>();

    private String search = "";
    private boolean searchFocused;
    private Module.Category dragCat;
    private int dragOffX, dragOffY;

    private int panelW = 98;
    private int rowH = 12;
    private int headerH = 14;
    private boolean mobile;

    public ClickGuiScreen() {
        super(Text.literal("Jay"));
    }

    private void ensureLayout() {
        mobile = this.width < 480 || this.height < 360;
        // Smaller panels (Meteor-like)
        panelW = mobile ? 88 : 100;
        rowH = mobile ? 12 : 13;
        headerH = 14;

        if (PANEL_POS.isEmpty() && JayHackClient.moduleManager != null) {
            int x = 8;
            int y = 26;
            for (Module.Category c : Module.Category.values()) {
                if (JayHackClient.moduleManager.getByCategory(c).isEmpty()) continue;
                PANEL_POS.put(c, new int[]{x, y});
                x += panelW + 6;
                if (x + panelW > this.width - 6) {
                    x = 8;
                    y += 100;
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
        int tw = 14;
        int th = 7;
        ctx.fill(x, y, x + tw, y + th, on ? ACCENT : TOGGLE_OFF);
        int kn = 5;
        int kx = on ? x + tw - kn - 1 : x + 1;
        ctx.fill(kx, y + 1, kx + kn, y + th - 1, 0xFFFFFFFF);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ensureLayout();

        // Light dim — world stays visible
        ctx.fill(0, 0, this.width, this.height, 0x55000000);

        int barH = 20;
        ctx.fill(0, 0, this.width, barH, 0xE0080810);
        ctx.fill(0, barH - 1, this.width, barH, 0x663DDCFF);
        JayLogo.draw(ctx, 3, 3, 12);
        ctx.drawTextWithShadow(textRenderer, "§bJay", 18, 6, TEXT);

        int sx = 42;
        int sw = Math.min(100, this.width / 5);
        ctx.fill(sx, 3, sx + sw, 17, 0xFF16161E);
        String sh = search.isEmpty() && !searchFocused ? "§8search" : "§f" + search + (searchFocused ? "§7|" : "");
        ctx.drawTextWithShadow(textRenderer, sh, sx + 3, 6, TEXT);

        int px = this.width - 6;
        for (int i = PROFILES.length - 1; i >= 0; i--) {
            String p = PROFILES[i];
            int pw = textRenderer.getWidth(p) + 6;
            px -= pw + 2;
            boolean on = p.equalsIgnoreCase(ClientSettings.lastProfile);
            ctx.fill(px, 3, px + pw, 17, on ? 0xFF1A2A32 : 0xFF121218);
            if (on) ctx.fill(px, 16, px + pw, 17, ACCENT);
            ctx.drawTextWithShadow(textRenderer, p, px + 3, 6, on ? ACCENT : TEXT_DIM);
        }

        if (JayHackClient.moduleManager == null) return;

        for (Module.Category cat : Module.Category.values()) {
            List<Module> mods = modulesOf(cat);
            if (mods.isEmpty()) continue;

            int[] pos = PANEL_POS.computeIfAbsent(cat, c -> new int[]{16, 36});
            int x = pos[0];
            int y = pos[1];
            int maxRows = mobile ? 9 : 12;
            int bodyH = Math.min(mods.size(), maxRows) * rowH + 1;
            int totalH = headerH + bodyH;

            ctx.fill(x + 2, y + 2, x + panelW + 2, y + totalH + 2, 0x40000000);
            fillRound(ctx, x, y, x + panelW, y + totalH, BG_PANEL);
            ctx.fill(x, y, x + panelW, y + headerH, BG_HEADER);
            ctx.fill(x, y, x + panelW, y + 1, ACCENT);
            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 3, y + 3, ACCENT);

            int row = 0;
            for (Module m : mods) {
                if (row >= maxRows) break;
                int ry = y + headerH + row * rowH;
                boolean hover = mouseX >= x && mouseX < x + panelW && mouseY >= ry && mouseY < ry + rowH;
                if (m.isEnabled() || hover) {
                    ctx.fill(x + 1, ry, x + panelW - 1, ry + rowH, m.isEnabled() ? BG_ROW_ON : BG_ROW);
                }
                String name = m.getName();
                int maxW = panelW - 24;
                if (textRenderer.getWidth(name) > maxW) {
                    while (textRenderer.getWidth(name + ".") > maxW && name.length() > 2) {
                        name = name.substring(0, name.length() - 1);
                    }
                    name = name + ".";
                }
                ctx.drawTextWithShadow(textRenderer, name, x + 3, ry + 2,
                        m.isEnabled() ? TEXT : TEXT_DIM);
                drawToggle(ctx, x + panelW - 18, ry + 2, m.isEnabled());
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

        int sx = 42;
        int sw = Math.min(100, this.width / 5);
        if (button == 0 && mouseY < 20 && mouseX >= sx && mouseX <= sx + sw) {
            searchFocused = true;
            return true;
        }

        if (button == 0 && mouseY < 20) {
            int px = this.width - 6;
            for (int i = PROFILES.length - 1; i >= 0; i--) {
                String p = PROFILES[i];
                int pw = textRenderer.getWidth(p) + 6;
                px -= pw + 2;
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
            int maxRows = mobile ? 9 : 12;

            if (button == 0 && mouseX >= x && mouseX < x + panelW && mouseY >= y && mouseY < y + headerH) {
                dragCat = cat;
                dragOffX = (int) mouseX - x;
                dragOffY = (int) mouseY - y;
                searchFocused = false;
                return true;
            }

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
                        } else m.toggle();
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
                pos[1] = Math.max(20, Math.min(this.height - 36, pos[1]));
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
