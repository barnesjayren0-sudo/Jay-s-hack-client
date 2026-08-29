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

/** Per-module settings panel — Boolean / Slider / Mode (Cyemer-inspired components). */
public class SettingsScreen extends Screen {

    private final Screen parent;
    private final Module module;
    private boolean listeningKey;

    private static final int ACCENT = 0xFF3DDCFF;
    private static final int BG = 0xF00E0E14;
    private static final int ROW = 0xFF14141C;

    public SettingsScreen(Screen parent, Module module) {
        super(Text.literal(module.getName()));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, 0x88000000);

        int w = Math.min(220, width - 24);
        int h = Math.min(280, height - 24);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, 0x44000000);
        ctx.fill(x, y, x + w, y + h, BG);
        ctx.fill(x, y, x + w, y + 18, 0xFF12121A);
        ctx.fill(x, y, x + w, y + 1, ACCENT);

        ctx.drawTextWithShadow(textRenderer, module.getName(), x + 8, y + 5, ACCENT);
        ctx.drawTextWithShadow(textRenderer, "×", x + w - 14, y + 5, 0xFF888888);

        int ry = y + 24;

        // Keybind row
        ctx.fill(x + 4, ry, x + w - 4, ry + 16, ROW);
        String keyLab = listeningKey ? "§e..." : (module.getKeyLabel().isEmpty() ? "None" : module.getKeyLabel());
        ctx.drawTextWithShadow(textRenderer, "Keybind", x + 8, ry + 4, 0xFFCCCCCC);
        ctx.drawTextWithShadow(textRenderer, keyLab, x + w - 8 - textRenderer.getWidth(keyLab), ry + 4, ACCENT);
        ry += 18;

        List<Setting> settings = module.getSettings();
        if (settings.isEmpty()) {
            ctx.drawTextWithShadow(textRenderer, "§7No extra settings", x + 8, ry + 4, 0xFF888888);
        } else {
            for (Setting s : settings) {
                if (ry + 16 > y + h - 8) break;
                ctx.fill(x + 4, ry, x + w - 4, ry + 16, ROW);
                ctx.drawTextWithShadow(textRenderer, s.getName(), x + 8, ry + 4, 0xFFCCCCCC);
                String val = s.getDisplayValue();
                ctx.drawTextWithShadow(textRenderer, val, x + w - 8 - textRenderer.getWidth(val), ry + 4, ACCENT);
                ry += 18;
            }
        }

        ctx.drawTextWithShadow(textRenderer, "§8LMB edit · ESC back", x + 8, y + h - 14, 0xFF666666);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();

        int w = Math.min(220, width - 24);
        int h = Math.min(280, height - 24);
        int x = (width - w) / 2;
        int y = (height - h) / 2;

        if (button == 0 && mx >= x + w - 18 && mx <= x + w - 4 && my >= y && my <= y + 18) {
            client.setScreen(parent);
            return true;
        }

        int ry = y + 24;
        // Keybind
        if (button == 0 && mx >= x + 4 && mx <= x + w - 4 && my >= ry && my < ry + 16) {
            listeningKey = true;
            return true;
        }
        ry += 18;

        for (Setting s : module.getSettings()) {
            if (ry + 16 > y + h - 8) break;
            if (button == 0 && mx >= x + 4 && mx <= x + w - 4 && my >= ry && my < ry + 16) {
                if (s instanceof BoolSetting b) b.toggle();
                else if (s instanceof ModeSetting m) m.cycle();
                else if (s instanceof NumberSetting n) {
                    // click right half + / left half -
                    if (mx > x + w / 2.0) n.increment();
                    else n.decrement();
                }
                return true;
            }
            ry += 18;
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
