package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.profile.PresetManager;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** First-run wizard: theme → preset → done. */
public class FirstLaunchScreen extends Screen {

    private int step = 0;
    private ThemeEngine.Theme picked = ThemeEngine.Theme.CYAN;
    private PresetManager.Preset preset = PresetManager.Preset.LEGIT;

    public FirstLaunchScreen() {
        super(Text.literal("Welcome"));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0xF00A0C11);
        int cx = width / 2;
        ctx.drawCenteredTextWithShadow(textRenderer, "§bWelcome to Jay Client", cx, 40, GuiTheme.TEXT);
        ctx.drawCenteredTextWithShadow(textRenderer, "§7v" + JayHackClient.VERSION + " · quick setup",
                cx, 54, GuiTheme.TEXT_DIM);

        if (step == 0) {
            ctx.drawCenteredTextWithShadow(textRenderer, "§f1/3 · Choose theme", cx, 80, GuiTheme.TEXT);
            drawThemeButtons(ctx, mouseX, mouseY);
        } else if (step == 1) {
            ctx.drawCenteredTextWithShadow(textRenderer, "§f2/3 · Starting preset", cx, 80, GuiTheme.TEXT);
            drawPresetButtons(ctx, mouseX, mouseY);
        } else {
            ctx.drawCenteredTextWithShadow(textRenderer, "§f3/3 · Ready", cx, 80, GuiTheme.TEXT);
            ctx.drawCenteredTextWithShadow(textRenderer, "§7RShift = GUI · .jay help · Del = Panic", cx, 110, GuiTheme.TEXT_DIM);
            drawButton(ctx, cx - 40, 150, 80, 18, "Finish", true);
        }
    }

    private void drawThemeButtons(DrawContext ctx, int mx, int my) {
        ThemeEngine.Theme[] themes = ThemeEngine.Theme.values();
        int startX = width / 2 - (themes.length * 54) / 2;
        for (int i = 0; i < themes.length; i++) {
            int x = startX + i * 54;
            boolean on = themes[i] == picked;
            drawButton(ctx, x, 110, 50, 18, themes[i].name(), on);
        }
        drawButton(ctx, width / 2 - 30, 150, 60, 18, "Next", true);
    }

    private void drawPresetButtons(DrawContext ctx, int mx, int my) {
        PresetManager.Preset[] ps = PresetManager.Preset.values();
        int startX = width / 2 - (ps.length * 70) / 2;
        for (int i = 0; i < ps.length; i++) {
            int x = startX + i * 70;
            boolean on = ps[i] == preset;
            drawButton(ctx, x, 110, 66, 18, ps[i].display, on);
        }
        drawButton(ctx, width / 2 - 30, 150, 60, 18, "Next", true);
    }

    private void drawButton(DrawContext ctx, int x, int y, int w, int h, String label, boolean on) {
        ctx.fill(x, y, x + w, y + h, on ? GuiTheme.ROW_ON : GuiTheme.PANEL2);
        int tw = textRenderer.getWidth(label);
        ctx.drawTextWithShadow(textRenderer, label, x + (w - tw) / 2, y + 5,
                on ? GuiTheme.ACCENT : GuiTheme.TEXT);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        double mx = click.x(), my = click.y();

        if (step == 0) {
            ThemeEngine.Theme[] themes = ThemeEngine.Theme.values();
            int startX = width / 2 - (themes.length * 54) / 2;
            for (int i = 0; i < themes.length; i++) {
                int x = startX + i * 54;
                if (mx >= x && mx <= x + 50 && my >= 110 && my <= 128) {
                    picked = themes[i];
                    ThemeEngine.apply(picked);
                    return true;
                }
            }
            if (mx >= width / 2.0 - 30 && mx <= width / 2.0 + 30 && my >= 150 && my <= 168) {
                step = 1;
                return true;
            }
        } else if (step == 1) {
            PresetManager.Preset[] ps = PresetManager.Preset.values();
            int startX = width / 2 - (ps.length * 70) / 2;
            for (int i = 0; i < ps.length; i++) {
                int x = startX + i * 70;
                if (mx >= x && mx <= x + 66 && my >= 110 && my <= 128) {
                    preset = ps[i];
                    return true;
                }
            }
            if (mx >= width / 2.0 - 30 && mx <= width / 2.0 + 30 && my >= 150 && my <= 168) {
                step = 2;
                return true;
            }
        } else {
            if (mx >= width / 2.0 - 40 && mx <= width / 2.0 + 40 && my >= 150 && my <= 168) {
                finish();
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private void finish() {
        ThemeEngine.apply(picked);
        PresetManager.apply(preset);
        ClientSettings.firstLaunchDone = true;
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        Notifications.push("Setup", "Welcome — " + preset.display);
        client.setScreen(null);
    }

    @Override
    public boolean shouldPause() { return false; }
}
