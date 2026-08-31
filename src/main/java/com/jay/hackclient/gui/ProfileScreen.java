package com.jay.hackclient.gui;

import com.jay.hackclient.config.ConfigIO;
import com.jay.hackclient.profile.PresetManager;
import com.jay.hackclient.profile.ProfileManager;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Profiles — presets, + New, confirm overwrite/delete, config export/import. */
public class ProfileScreen extends Screen {

    private final Screen parent;
    private String nameBuf = "Custom";
    private boolean typing;
    private String pendingDelete;
    private String pendingOverwrite;

    public ProfileScreen(Screen parent) {
        super(Text.literal("Profiles"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, GuiTheme.OVERLAY);
        int w = Math.min(300, width - 16);
        int x = (width - w) / 2;
        int y = 20;
        int h = Math.min(height - 32, 260);
        ctx.fill(x, y, x + w, y + h, GuiTheme.BG);
        ctx.drawTextWithShadow(textRenderer, "§bProfiles", x + 8, y + 6, GuiTheme.TEXT);

        ctx.drawTextWithShadow(textRenderer, "§7Presets", x + 8, y + 20, GuiTheme.TEXT_DIM);
        int px = x + 8;
        int py = y + 32;
        for (PresetManager.Preset p : PresetManager.Preset.values()) {
            int bw = textRenderer.getWidth(p.display) + 10;
            if (px + bw > x + w - 8) { px = x + 8; py += 16; }
            ctx.fill(px, py, px + bw, py + 14, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, p.display, px + 5, py + 3, GuiTheme.ACCENT);
            px += bw + 4;
        }

        int fieldY = py + 20;
        ctx.drawTextWithShadow(textRenderer, "§7Name (tap)", x + 8, fieldY, GuiTheme.TEXT_DIM);
        ctx.fill(x + 8, fieldY + 11, x + w - 8, fieldY + 26, typing ? GuiTheme.ROW_ON : GuiTheme.PANEL2);
        String shown = typing ? nameBuf + "§7|" : nameBuf;
        ctx.drawTextWithShadow(textRenderer, shown, x + 12, fieldY + 15, GuiTheme.TEXT);

        int by = fieldY + 32;
        drawBtn(ctx, x + 8, by, 50, "+ New");
        drawBtn(ctx, x + 62, by, 50, "Save");
        drawBtn(ctx, x + 116, by, 50, "Load");
        drawBtn(ctx, x + 170, by, 50, "Delete");

        drawBtn(ctx, x + 8, by + 18, 60, "Export");
        drawBtn(ctx, x + 72, by + 18, 60, "Import");

        int listY = by + 40;
        if (pendingDelete != null) {
            ctx.drawTextWithShadow(textRenderer, "§cDelete " + pendingDelete + "?", x + 8, listY, GuiTheme.DANGER);
            drawBtn(ctx, x + 8, listY + 14, 50, "Yes");
            drawBtn(ctx, x + 62, listY + 14, 50, "No");
        } else if (pendingOverwrite != null) {
            ctx.drawTextWithShadow(textRenderer, "§eOverwrite " + pendingOverwrite + "?", x + 8, listY, GuiTheme.TEXT);
            drawBtn(ctx, x + 8, listY + 14, 50, "Yes");
            drawBtn(ctx, x + 62, listY + 14, 50, "No");
        } else {
            ctx.drawTextWithShadow(textRenderer, "§7Saved", x + 8, listY, GuiTheme.TEXT_DIM);
            int ly = listY + 14;
            for (String n : ProfileManager.list()) {
                ctx.drawTextWithShadow(textRenderer, "§f" + n, x + 12, ly, GuiTheme.TEXT);
                ly += 12;
                if (ly > y + h - 8) break;
            }
        }
    }

    private void drawBtn(DrawContext ctx, int x, int y, int w, String t) {
        ctx.fill(x, y, x + w, y + 14, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, t, x + 6, y + 3, GuiTheme.TEXT);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int w = Math.min(300, width - 16);
        int x = (width - w) / 2;
        int y = 20;
        double mx = click.x(), my = click.y();

        // Recompute layout anchors (same as render)
        int px = x + 8;
        int py = y + 32;
        for (PresetManager.Preset p : PresetManager.Preset.values()) {
            int bw = textRenderer.getWidth(p.display) + 10;
            if (px + bw > x + w - 8) { px = x + 8; py += 16; }
            if (mx >= px && mx <= px + bw && my >= py && my <= py + 14) {
                PresetManager.apply(p);
                return true;
            }
            px += bw + 4;
        }

        int fieldY = py + 20;
        if (mx >= x + 8 && mx <= x + w - 8 && my >= fieldY + 11 && my <= fieldY + 26) {
            typing = true;
            return true;
        }

        int by = fieldY + 32;
        int listY = by + 40;

        if (pendingDelete != null) {
            if (mx >= x + 8 && mx <= x + 58 && my >= listY + 14 && my <= listY + 28) {
                ProfileManager.delete(pendingDelete);
                pendingDelete = null;
                return true;
            }
            if (mx >= x + 62 && mx <= x + 112 && my >= listY + 14 && my <= listY + 28) {
                pendingDelete = null;
                return true;
            }
            return true;
        }
        if (pendingOverwrite != null) {
            if (mx >= x + 8 && mx <= x + 58 && my >= listY + 14 && my <= listY + 28) {
                ProfileManager.save(pendingOverwrite);
                pendingOverwrite = null;
                return true;
            }
            if (mx >= x + 62 && mx <= x + 112 && my >= listY + 14 && my <= listY + 28) {
                pendingOverwrite = null;
                return true;
            }
            return true;
        }

        if (my >= by && my <= by + 14) {
            if (mx >= x + 8 && mx <= x + 58) {
                nameBuf = "Profile" + (ProfileManager.list().size() + 1);
                typing = true;
                Notifications.push("Profile", "Tap name field, then Save");
                return true;
            }
            if (mx >= x + 62 && mx <= x + 112) {
                if (ProfileManager.list().stream().anyMatch(n -> n.equalsIgnoreCase(nameBuf))) {
                    pendingOverwrite = nameBuf;
                } else {
                    ProfileManager.save(nameBuf);
                }
                return true;
            }
            if (mx >= x + 116 && mx <= x + 166) {
                ProfileManager.load(nameBuf);
                return true;
            }
            if (mx >= x + 170 && mx <= x + 220) {
                pendingDelete = nameBuf;
                return true;
            }
        }

        if (my >= by + 18 && my <= by + 32) {
            if (mx >= x + 8 && mx <= x + 68) {
                ConfigIO.exportToClipboard();
                return true;
            }
            if (mx >= x + 72 && mx <= x + 132) {
                ConfigIO.importFromClipboard();
                return true;
            }
        }

        int ly = listY + 14;
        for (String n : ProfileManager.list()) {
            if (mx >= x + 12 && mx <= x + w - 12 && my >= ly && my <= ly + 12) {
                nameBuf = n;
                typing = false;
                return true;
            }
            ly += 12;
        }

        typing = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (pendingDelete != null || pendingOverwrite != null) {
                pendingDelete = null;
                pendingOverwrite = null;
                return true;
            }
            client.setScreen(parent);
            return true;
        }
        if (typing && input.key() == GLFW.GLFW_KEY_BACKSPACE && !nameBuf.isEmpty()) {
            nameBuf = nameBuf.substring(0, nameBuf.length() - 1);
            return true;
        }
        if (typing && input.key() == GLFW.GLFW_KEY_ENTER) {
            typing = false;
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (typing) {
            char c = (char) input.codepoint();
            if ((Character.isLetterOrDigit(c) || c == '_' || c == '-') && nameBuf.length() < 24) {
                nameBuf += c;
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
