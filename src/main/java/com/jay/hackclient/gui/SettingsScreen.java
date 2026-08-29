package com.jay.hackclient.gui;

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
        int rows = 5 + settings.size();
        int h = Math.min(24 + rows * rowH + 28, height - 24);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        ctx.fill(x + 3, y + 4, x + w + 3, y + h + 4, GuiTheme.SHADOW);
        panel(ctx, x, y, x + w, y + h, GuiTheme.BG);
        ctx.fill(x, y, x + w, y + 1, GuiTheme.ACCENT);
        ctx.fill(x, y, x + w, y + 22, GuiTheme.PANEL);

        ctx.drawTextWithShadow(textRenderer, module.getName(), x + 10, y + 7, GuiTheme.TEXT);
        ctx.drawTextWithShadow(textRenderer, "×", x + w - 14, y + 7, GuiTheme.TEXT_DIM);

        int ry = y + 28;

        panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
        String keyLab = listeningKey ? "§ePress key..."
                : (module.getKeyLabel().isEmpty() ? "None" : module.getKeyLabel());
        ctx.drawTextWithShadow(textRenderer, "Keybind", x + 14, ry + 6, GuiTheme.TEXT_DIM);
        ctx.drawTextWithShadow(textRenderer, keyLab,
                x + w - 14 - textRenderer.getWidth(keyLab), ry + 6, GuiTheme.ACCENT);
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
        ctx.drawTextWithShadow(textRenderer, "ChatFeedback", x + 14, ry + 6, GuiTheme.TEXT_DIM);
        String cf = module.isChatFeedback() ? "ON" : "OFF";
        ctx.drawTextWithShadow(textRenderer, cf,
                x + w - 14 - textRenderer.getWidth(cf), ry + 6, GuiTheme.ACCENT);
        ry += rowH;

        if (settings.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "No extra settings",
                    x + 14, ry + 6, GuiTheme.TEXT_DIM);
        } else {
            int idx = 0;
            for (Setting s : settings) {
                if (idx++ < scroll) continue;
                if (ry + rowH > y + h - 22) break;
                panel(ctx, x + 8, ry, x + w - 8, ry + rowH - 4, GuiTheme.PANEL2);
                ctx.drawTextWithShadow(textRenderer, s.getName(), x + 14, ry + 6, GuiTheme.TEXT);
                if (s instanceof NumberSetting n) {
                    int barX = x + w / 2;
                    int barW = w / 2 - 24;
                    int barY = ry + rowH / 2 - 2;
                    ctx.fill(barX, barY, barX + barW, barY + 3, GuiTheme.TOGGLE_OFF);
                    double pct = (n.get() - n.getMin()) / Math.max(0.0001, n.getMax() - n.getMin());
                    int fill = (int) (barW * pct);
                    ctx.fill(barX, barY, barX + fill, barY + 3, GuiTheme.ACCENT);
                    ctx.fill(barX + fill - 2, barY - 2, barX + fill + 2, barY + 5, 0xFFFFFFFF);
                    String val = n.getDisplayValue();
                    ctx.drawTextWithShadow(textRenderer, val,
                            x + w - 14 - textRenderer.getWidth(val), ry + 6, GuiTheme.ACCENT);
                } else if (s instanceof BoolSetting b) {
                    int tw = 28, th = 12;
                    int tx = x + w - 14 - tw;
                    int ty = ry + (rowH - 4 - th) / 2;
                    ctx.fill(tx + 1, ty, tx + tw - 1, ty + th, b.get() ? GuiTheme.ACCENT : GuiTheme.TOGGLE_OFF);
                    ctx.fill(tx, ty + 1, tx + tw, ty + th - 1, b.get() ? GuiTheme.ACCENT : GuiTheme.TOGGLE_OFF);
                    int kn = th - 4;
                    int kx = b.get() ? tx + tw - kn - 2 : tx + 2;
                    ctx.fill(kx, ty + 2, kx + kn, ty + 2 + kn, 0xFFFFFFFF);
                } else {
                    String val = s.getDisplayValue();
                    ctx.drawTextWithShadow(textRenderer, val,
                            x + w - 14 - textRenderer.getWidth(val), ry + 6, GuiTheme.ACCENT);
                }
                ry += rowH;
            }
        }

        ctx.drawTextWithShadow(textRenderer, "LMB edit · ESC back",
                x + 10, y + h - 14, GuiTheme.TEXT_DIM);
        super.render(ctx, mouseX, mouseY, delta);
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
        int rows = 5 + settings.size();
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
            module.setKeyMode(module.getKeyMode() == Module.KeyMode.TOGGLE
                    ? Module.KeyMode.HOLD : Module.KeyMode.TOGGLE);
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

        int idx = 0;
        for (Setting s : settings) {
            if (idx++ < scroll) continue;
            if (ry + rowH > y + h - 22) break;
            if (button == 0 && mx >= x + 8 && mx <= x + w - 8 && my >= ry && my < ry + rowH - 4) {
                if (s instanceof BoolSetting b) b.toggle();
                else if (s instanceof ModeSetting m) m.cycle();
                else if (s instanceof NumberSetting n) {
                    int barX = x + w / 2;
                    int barW = w / 2 - 24;
                    if (mx >= barX && mx <= barX + barW) {
                        double pct = (mx - barX) / (double) barW;
                        n.set(n.getMin() + pct * (n.getMax() - n.getMin()));
                    } else if (mx > x + w / 2.0) n.increment();
                    else n.decrement();
                }
                return true;
            }
            ry += rowH;
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
