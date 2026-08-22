package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class SettingsScreen extends Screen {

    private final Screen parent;
    private final Module module;
    private int scroll;

    private static final int ACCENT = 0xFFB24BF3;
    private static final String[] ROWS = {
            "aimMode", "targetPriority", "velocityMode",
            "aimRange", "aimFov", "aimSmooth", "auraRange",
            "hitboxExpand", "missChance", "velHorizontal",
            "potSlotMin", "potSlotMax", "requireAttackKey",
            "Favorite module", "Save & Back"
    };

    public SettingsScreen(Screen parent, Module module) {
        super(Text.literal("Settings"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int w = Math.min(300, this.width - 20);
        int h = Math.min(280, this.height - 20);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;

        ctx.fill(0, 0, this.width, this.height, 0xAA000000);
        ctx.fill(x, y, x + w, y + h, 0xF0121218);
        ctx.fill(x, y, x + w, y + 22, 0xF0161620);
        ctx.fill(x, y + 21, x + w, y + 22, ACCENT);

        String title = module != null ? module.getName() + " settings" : "Settings";
        ctx.drawTextWithShadow(textRenderer, "§d" + title, x + 8, y + 7, 0xFFFFFF);

        int rowH = 17;
        int top = y + 26;
        for (int i = 0; i < ROWS.length; i++) {
            if (i < scroll) continue;
            int di = i - scroll;
            int ry = top + di * rowH;
            if (ry + rowH > y + h - 8) break;
            boolean hover = mouseX >= x + 4 && mouseX <= x + w - 4 && mouseY >= ry && mouseY < ry + rowH;
            if (hover) ctx.fill(x + 4, ry, x + w - 4, ry + rowH - 1, 0x22FFFFFF);
            String label = ROWS[i];
            String val = valueOf(label);
            ctx.drawTextWithShadow(textRenderer, label, x + 10, ry + 3, 0xFFCCCCDD);
            int vw = textRenderer.getWidth(val);
            ctx.drawTextWithShadow(textRenderer, val, x + w - 12 - vw, ry + 3, ACCENT);
        }
        ctx.drawTextWithShadow(textRenderer, "§8Click to change",
                x + 8, y + h - 12, 0xFF666677);
        super.render(ctx, mouseX, mouseY, delta);
    }

    private String valueOf(String key) {
        return switch (key) {
            case "aimMode" -> ClientSettings.aimMode;
            case "targetPriority" -> ClientSettings.targetPriority;
            case "velocityMode" -> ClientSettings.velocityMode;
            case "aimRange" -> String.format("%.2f", ClientSettings.aimRange);
            case "aimFov" -> String.format("%.0f", ClientSettings.aimFov);
            case "aimSmooth" -> String.format("%.2f", ClientSettings.aimSmooth);
            case "auraRange" -> String.format("%.2f", ClientSettings.auraRange);
            case "hitboxExpand" -> String.format("%.2f", ClientSettings.hitboxExpand);
            case "missChance" -> String.valueOf(ClientSettings.missChance);
            case "velHorizontal" -> String.format("%.2f", ClientSettings.velocityHorizontal);
            case "potSlotMin" -> String.valueOf(ClientSettings.potSlotMin);
            case "potSlotMax" -> String.valueOf(ClientSettings.potSlotMax);
            case "requireAttackKey" -> String.valueOf(ClientSettings.requireAttackKey);
            case "Favorite module" -> module != null && ClientSettings.isFavorite(module.getName()) ? "★ yes" : "☆ no";
            case "Save & Back" -> "§aOK";
            default -> "?";
        };
    }

    private void cycle(String key) {
        switch (key) {
            case "aimMode" ->
                    ClientSettings.aimMode = "silent".equalsIgnoreCase(ClientSettings.aimMode) ? "classic" : "silent";
            case "targetPriority" -> {
                String p = ClientSettings.targetPriority;
                if ("closest".equals(p)) ClientSettings.targetPriority = "lowest_hp";
                else if ("lowest_hp".equals(p)) ClientSettings.targetPriority = "crosshair";
                else ClientSettings.targetPriority = "closest";
            }
            case "velocityMode" -> {
                String m = ClientSettings.velocityMode;
                if ("soft".equals(m)) ClientSettings.applyVelocityMode("medium");
                else if ("medium".equals(m)) ClientSettings.applyVelocityMode("strong");
                else ClientSettings.applyVelocityMode("soft");
            }
            case "aimRange" -> ClientSettings.aimRange = next(ClientSettings.aimRange, 3.0, 6.0, 0.25);
            case "aimFov" -> ClientSettings.aimFov = (float) next(ClientSettings.aimFov, 40, 180, 5);
            case "aimSmooth" -> ClientSettings.aimSmooth = (float) next(ClientSettings.aimSmooth, 0.08, 0.5, 0.02);
            case "auraRange" -> ClientSettings.auraRange = next(ClientSettings.auraRange, 3.0, 4.5, 0.1);
            case "hitboxExpand" -> {
                ClientSettings.hitboxExpand = next(ClientSettings.hitboxExpand, 0.0, 0.35, 0.02);
                Hitboxes.setExpand(ClientSettings.hitboxExpand);
            }
            case "missChance" -> ClientSettings.missChance = (int) next(ClientSettings.missChance, 0, 20, 1);
            case "velHorizontal" ->
                    ClientSettings.velocityHorizontal = next(ClientSettings.velocityHorizontal, 0.25, 0.9, 0.05);
            case "potSlotMin" -> ClientSettings.potSlotMin = (int) next(ClientSettings.potSlotMin, 0, 8, 1);
            case "potSlotMax" -> ClientSettings.potSlotMax = (int) next(ClientSettings.potSlotMax, 0, 8, 1);
            case "requireAttackKey" -> ClientSettings.requireAttackKey = !ClientSettings.requireAttackKey;
            case "Favorite module" -> {
                if (module != null) ClientSettings.toggleFavorite(module.getName());
            }
            case "Save & Back" -> {
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                close();
            }
            default -> {}
        }
        if (JayHackClient.configManager != null && !"Save & Back".equals(key)) {
            JayHackClient.configManager.save();
        }
    }

    private double next(double cur, double min, double max, double step) {
        double n = cur + step;
        if (n > max + 1e-6) return min;
        return Math.round(n * 100.0) / 100.0;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int w = Math.min(300, this.width - 20);
        int h = Math.min(280, this.height - 20);
        int x = (this.width - w) / 2;
        int y = (this.height - h) / 2;
        int rowH = 17;
        int top = y + 26;
        double mx = click.x(), my = click.y();
        for (int i = 0; i < ROWS.length; i++) {
            if (i < scroll) continue;
            int di = i - scroll;
            int ry = top + di * rowH;
            if (ry + rowH > y + h - 8) break;
            if (mx >= x + 4 && mx <= x + w - 4 && my >= ry && my < ry + rowH) {
                cycle(ROWS[i]);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
