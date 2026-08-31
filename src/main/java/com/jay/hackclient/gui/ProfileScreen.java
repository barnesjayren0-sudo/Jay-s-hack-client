package com.jay.hackclient.gui;

import com.jay.hackclient.profile.PresetManager;
import com.jay.hackclient.profile.ProfileManager;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/** Profiles UI: presets + save/load custom. */
public class ProfileScreen extends Screen {

    private final Screen parent;
    private String nameBuf = "Custom";
    private boolean typing;

    public ProfileScreen(Screen parent) {
        super(Text.literal("Profiles"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, GuiTheme.OVERLAY);
        int w = Math.min(300, width - 16);
        int x = (width - w) / 2;
        int y = 30;
        ctx.fill(x, y, x + w, y + 220, GuiTheme.BG);
        ctx.drawTextWithShadow(textRenderer, "§bProfiles", x + 8, y + 8, GuiTheme.TEXT);

        ctx.drawTextWithShadow(textRenderer, "§7Presets", x + 8, y + 24, GuiTheme.TEXT_DIM);
        int px = x + 8;
        for (PresetManager.Preset p : PresetManager.Preset.values()) {
            int bw = textRenderer.getWidth(p.display) + 10;
            ctx.fill(px, y + 36, px + bw, y + 50, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, p.display, px + 5, y + 39, GuiTheme.ACCENT);
            px += bw + 4;
        }

        ctx.drawTextWithShadow(textRenderer, "§7Custom name", x + 8, y + 60, GuiTheme.TEXT_DIM);
        ctx.fill(x + 8, y + 72, x + w - 8, y + 86, GuiTheme.PANEL2);
        String shown = typing ? nameBuf + "§7|" : nameBuf;
        ctx.drawTextWithShadow(textRenderer, shown, x + 12, y + 75, GuiTheme.TEXT);

        drawBtn(ctx, x + 8, y + 94, 60, "Save");
        drawBtn(ctx, x + 72, y + 94, 60, "Load");
        drawBtn(ctx, x + 136, y + 94, 60, "Delete");

        ctx.drawTextWithShadow(textRenderer, "§7Saved", x + 8, y + 118, GuiTheme.TEXT_DIM);
        List<String> list = ProfileManager.list();
        int ly = y + 132;
        for (String n : list) {
            ctx.drawTextWithShadow(textRenderer, "§f" + n, x + 12, ly, GuiTheme.TEXT);
            ly += 11;
            if (ly > y + 210) break;
        }
        if (list.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "§8No profiles yet", x + 12, ly, GuiTheme.MUTED);
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
        int y = 30;
        double mx = click.x(), my = click.y();

        int px = x + 8;
        for (PresetManager.Preset p : PresetManager.Preset.values()) {
            int bw = textRenderer.getWidth(p.display) + 10;
            if (mx >= px && mx <= px + bw && my >= y + 36 && my <= y + 50) {
                PresetManager.apply(p);
                return true;
            }
            px += bw + 4;
        }

        if (mx >= x + 8 && mx <= x + w - 8 && my >= y + 72 && my <= y + 86) {
            typing = true;
            return true;
        }
        if (my >= y + 94 && my <= y + 108) {
            if (mx >= x + 8 && mx <= x + 68) { ProfileManager.save(nameBuf); return true; }
            if (mx >= x + 72 && mx <= x + 132) { ProfileManager.load(nameBuf); return true; }
            if (mx >= x + 136 && mx <= x + 196) { ProfileManager.delete(nameBuf); return true; }
        }

        List<String> list = ProfileManager.list();
        int ly = y + 132;
        for (String n : list) {
            if (mx >= x + 12 && mx <= x + w - 12 && my >= ly && my <= ly + 11) {
                nameBuf = n;
                return true;
            }
            ly += 11;
        }
        typing = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        if (typing && input.key() == GLFW.GLFW_KEY_BACKSPACE && !nameBuf.isEmpty()) {
            nameBuf = nameBuf.substring(0, nameBuf.length() - 1);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (typing) {
            char c = (char) input.codepoint();
            if (c >= 32 && c < 127 && nameBuf.length() < 24) {
                nameBuf += c;
                return true;
            }
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
