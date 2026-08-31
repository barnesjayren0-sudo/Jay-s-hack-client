package com.jay.hackclient.gui;

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

/** Profiles UI — presets, + New, confirm overwrite/delete (mobile-friendly). */
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
        int y = 24;
        int h = Math.min(height - 40, 240);
        ctx.fill(x, y, x + w, y + h, GuiTheme.BG);
        ctx.drawTextWithShadow(textRenderer, "§bProfiles", x + 8, y + 8, GuiTheme.TEXT);

        ctx.drawTextWithShadow(textRenderer, "§7Presets", x + 8, y + 22, GuiTheme.TEXT_DIM);
        int px = x + 8;
        int py = y + 34;
        for (PresetManager.Preset p : PresetManager.Preset.values()) {
            int bw = textRenderer.getWidth(p.display) + 10;
            if (px + bw > x + w - 8) { px = x + 8; py += 16; }
            ctx.fill(px, py, px + bw, py + 14, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, p.display, px + 5, py + 3, GuiTheme.ACCENT);
            px += bw + 4;
        }

        int fieldY = py + 22;
        ctx.drawTextWithShadow(textRenderer, "§7Name (tap to type)", x + 8, fieldY, GuiTheme.TEXT_DIM);
        ctx.fill(x + 8, fieldY + 12, x + w - 8, fieldY + 28, typing ? GuiTheme.ROW_ON : GuiTheme.PANEL2);
        String shown = typing ? nameBuf + "§7|" : nameBuf;
        ctx.drawTextWithShadow(textRenderer, shown, x + 12, fieldY + 16, GuiTheme.TEXT);

        int by = fieldY + 36;
        drawBtn(ctx, x + 8, by, 52, "+ New");
        drawBtn(ctx, x + 64, by, 52, "Save");
        drawBtn(ctx, x + 120, by, 52, "Load");
        drawBtn(ctx, x + 176, by, 52, "Delete");

        if (pendingDelete != null) {
            ctx.drawTextWithShadow(textRenderer, "§cDelete " + pendingDelete + "?", x + 8, by + 20, GuiTheme.DANGER);
            drawBtn(ctx, x + 8, by + 34, 50, "Yes");
            drawBtn(ctx, x + 62, by + 34, 50, "No");
        } else if (pendingOverwrite != null) {
            ctx.drawTextWithShadow(textRenderer, "§eOverwrite " + pendingOverwrite + "?", x + 8, by + 20, GuiTheme.TEXT);
            drawBtn(ctx, x + 8, by + 34, 50, "Yes");
            drawBtn(ctx, x + 62, by + 34, 50, "No");
        } else {
            ctx.drawTextWithShadow(textRenderer, "§7Saved", x + 8, by + 20, GuiTheme.TEXT_DIM);
            List<String> list = ProfileManager.list();
            int ly = by + 34;
            for (String n : list) {
                boolean sel = n.equalsIgnoreCase(nameBuf);
                if (sel) ctx.fill(x + 8, ly - 1, x + w - 8, ly + 11, GuiTheme.ROW_HOVER);
                ctx.drawTextWithShadow(textRenderer, (sel ? "§b" : "§f") + n, x + 12, ly, GuiTheme.TEXT);
                ly += 12;
                if (ly > y + h - 12) break;
            }
            if (list.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "§8No profiles yet", x + 12, ly, GuiTheme.MUTED);
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
        int y = 24;
        double mx = click.x(), my = click.y();

        int px = x + 8;
        int py = y + 34;
        for (PresetManager.Preset p : PresetManager.Preset.values()) {
            int bw = textRenderer.getWidth(p.display) + 10;
            if (px + bw > x + w - 8) { px = x + 8; py += 16; }
            if (mx >= px && mx <= px + bw && my >= py && my <= py + 14) {
                PresetManager.apply(p);
                return true;
            }
            px += bw + 4;
        }

        int fieldY = py + 22;
        if (mx >= x + 8 && mx <= x + w - 8 && my >= fieldY + 12 && my <= fieldY + 28) {
            typing = true;
            return true;
        }

        int by = fieldY + 36;

        if (pendingDelete != null) {
            if (mx >= x + 8 && mx <= x + 58 && my >= by + 34 && my <= by + 48) {
                ProfileManager.delete(pendingDelete);
                pendingDelete = null;
                return true;
            }
            if (mx >= x + 62 && mx <= x + 112 && my >= by + 34 && my <= by + 48) {
                pendingDelete = null;
                return true;
            }
            return true;
        }
        if (pendingOverwrite != null) {
            if (mx >= x + 8 && mx <= x + 58 && my >= by + 34 && my <= by + 48) {
                ProfileManager.save(pendingOverwrite);
                pendingOverwrite = null;
                return true;
            }
            if (mx >= x + 62 && mx <= x + 112 && my >= by + 34 && my <= by + 48) {
                pendingOverwrite = null;
                return true;
            }
            return true;
        }

        if (my >= by && my <= by + 14) {
            if (mx >= x + 8 && mx <= x + 60) {
                nameBuf = "Profile" + (ProfileManager.list().size() + 1);
                typing = true;
                Notifications.push("Profile", "Name set — Save when ready");
                return true;
            }
            if (mx >= x + 64 && mx <= x + 116) {
                if (ProfileManager.list().stream().anyMatch(n -> n.equalsIgnoreCase(nameBuf))) {
                    pendingOverwrite = nameBuf;
                } else {
                    ProfileManager.save(nameBuf);
                }
                return true;
            }
            if (mx >= x + 120 && mx <= x + 172) {
                ProfileManager.load(nameBuf);
                return true;
            }
            if (mx >= x + 176 && mx <= x + 228) {
                pendingDelete = nameBuf;
                return true;
            }
        }

        List<String> list = ProfileManager.list();
        int ly = by + 34;
        for (String n : list) {
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
