package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.module.setting.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class SettingsScreen extends Screen {

    private final Screen parent;
    private final Module module;
    private boolean listeningKey;
    private int scroll;

    public SettingsScreen(Screen parent, Module module) {
        super(Text.literal(module.getName()));
        this.parent = parent;
        this.module = module;
    }

    private void panel(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, GuiTheme.OVERLAY);

        boolean mobile = width < 480;
        int w = Math.min(mobile ? width - 20 : 260, width - 20);
        int rowH = mobile ? 28 : 26;
        List<Setting> settings = module.getSettings();
        int rows = 6 + settings.size();
        int h = Math.min(24 + rows * rowH + 28, height - 24);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        ctx.fill(x + 3, y + 4, x + w + 3, y + h + 4, GuiTheme.SHADOW);
        panel(ctx, x, y, x + w, y + h, GuiTheme.BG);

        ctx.drawTextWithShadow(textRenderer, module.getName(), x + 10, y + 7, GuiTheme.TEXT);
        ctx.drawTextWithShadow(textRenderer, "×", x + w - 14, y + 7, GuiTheme.TEXT_DIM);

        int ry = y + 28;

        panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, "Keybind", x + 14, ry + 6, GuiTheme.TEXT_DIM);
        String keyLab = listeningKey ? "§ePress key..." : (module.getKeyLabel().isEmpty() ? "None" : module.getKeyLabel());
        ctx.drawTextWithShadow(textRenderer, keyLab,
                x + w - 14 - textRenderer.getWidth(keyLab.replaceAll("§.", "")), ry + 6, GuiTheme.ACCENT);
        ry += rowH;

        panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, "KeyMode", x + 14, ry + 6, GuiTheme.TEXT_DIM);
        String km = module.getKeyMode().name();
        ctx.drawTextWithShadow(textRenderer, km,
                x + w - 14 - textRenderer.getWidth(km), ry + 6, GuiTheme.ACCENT);
        ry += rowH;

        panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, "Drawn", x + 14, ry + 6, GuiTheme.TEXT_DIM);
        String dw = module.isDrawn() ? "ON" : "OFF";
        ctx.drawTextWithShadow(textRenderer, dw,
                x + w - 14 - textRenderer.getWidth(dw), ry + 6, GuiTheme.ACCENT);
        ry += rowH;

        panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, "Chat FB", x + 14, ry + 6, GuiTheme.TEXT_DIM);
        String cf = module.isChatFeedback() ? "ON" : "OFF";
        ctx.drawTextWithShadow(textRenderer, cf,
                x + w - 14 - textRenderer.getWidth(cf), ry + 6, GuiTheme.ACCENT);
        ry += rowH;

        for (Setting s : settings) {
            if (ry + rowH > y + h - 40) break;
            panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
            ctx.drawTextWithShadow(textRenderer, s.getName(), x + 14, ry + 6, GuiTheme.TEXT_DIM);
            String val = s.getDisplayValue();
            if (s instanceof NumberSetting n) {
                int barX = x + w / 2;
                int barW = w / 2 - 20;
                int barY = ry + rowH / 2 - 2;
                ctx.fill(barX, barY, barX + barW, barY + 4, 0xFF333344);
                double t = (n.get() - n.getMin()) / Math.max(0.001, n.getMax() - n.getMin());
                ctx.fill(barX, barY, barX + (int) (barW * t), barY + 4, GuiTheme.ACCENT);
            }
            ctx.drawTextWithShadow(textRenderer, val,
                    x + w - 14 - textRenderer.getWidth(val), ry + 6, GuiTheme.ACCENT);
            ry += rowH;
        }

        panel(ctx, x + 8, y + h - 34, x + w - 8, y + h - 12, GuiTheme.PANEL2);
        ctx.drawTextWithShadow(textRenderer, "§cReset defaults", x + 14, y + h - 28, 0xFFFF5555);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        boolean mobile = width < 480;
        int w = Math.min(mobile ? width - 20 : 260, width - 20);
        int rowH = mobile ? 28 : 26;
        List<Setting> settings = module.getSettings();
        int rows = 6 + settings.size();
        int h = Math.min(24 + rows * rowH + 28, height - 24);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        if (button == 0 && mx >= x + w - 18 && mx <= x + w - 4 && my >= y && my <= y + 22) {
            client.setScreen(parent);
            return true;
        }

        int ry = y + 28;
        if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= ry && my < ry + rowH - 4) {
            listeningKey = true;
            return true;
        }
        ry += rowH;
        if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= ry && my < ry + rowH - 4) {
            Module.KeyMode next = module.getKeyMode() == Module.KeyMode.TOGGLE
                    ? Module.KeyMode.HOLD : Module.KeyMode.TOGGLE;
            module.setKeyMode(next);
            return true;
        }
        ry += rowH;
        if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= ry && my < ry + rowH - 4) {
            module.setDrawn(!module.isDrawn());
            return true;
        }
        ry += rowH;
        if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= ry && my < ry + rowH - 4) {
            module.setChatFeedback(!module.isChatFeedback());
            return true;
        }
        ry += rowH;

        for (Setting s : settings) {
            if (ry + rowH > y + h - 40) break;
            if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= ry && my < ry + rowH - 4) {
                if (s instanceof BoolSetting b) b.toggle();
                else if (s instanceof ModeSetting md) md.cycle();
                else if (s instanceof NumberSetting n) {
                    int barX = x + w / 2;
                    int barW = w / 2 - 20;
                    double t = Math.max(0, Math.min(1, (mx - barX) / barW));
                    n.set(n.getMin() + t * (n.getMax() - n.getMin()));
                }
                return true;
            }
            ry += rowH;
        }

        if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= y + h - 34 && my <= y + h - 12) {
            module.resetSettings();
            try {
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            } catch (Throwable ignored) {}
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (listeningKey) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_DELETE) {
                module.setKeyBind(-1);
            } else {
                module.setKeyBind(key);
            }
            listeningKey = false;
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
