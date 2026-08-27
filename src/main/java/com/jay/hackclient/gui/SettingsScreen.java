package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.module.modules.Reach;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Settings — target priority + all combat sliders persist on change. */
public class SettingsScreen extends Screen {

    private static final int BG_OVERLAY = 0x99000000;
    private static final int BG_WINDOW = 0xFF0E0E14;
    private static final int BG_ROW = 0xFF14141C;
    private static final int ACCENT = 0xFF3DDCFF;
    private static final int TEXT = 0xFFF0F0F8;
    private static final int TEXT_DIM = 0xFF7A7A90;
    private static final int TRACK = 0xFF2A2A38;
    private static final int DIVIDER = 0x14FFFFFF;

    private final Screen parent;
    private final Module module;
    private int scroll;
    private int dragging = -1;

    private int winX, winY, winW, winH;
    private int headerH = 36;
    private int rowH = 40;

    private static final String[] ROWS = {
            "aimMode",
            "targetPriority",
            "velocityMode",
            "aimRange",
            "aimFov",
            "aimSmooth",
            "auraRange",
            "reachDistance",
            "hitboxExpand",
            "missChance",
            "velHorizontal",
            "potSlotMin",
            "potSlotMax",
            "requireAttackKey",
            "Favorite",
            "Save & Back"
    };

    public SettingsScreen(Screen parent, Module module) {
        super(Text.literal("Settings"));
        this.parent = parent;
        this.module = module;
    }

    private void computeLayout() {
        boolean small = this.width < 480 || this.height < 340;
        winW = small ? Math.max(this.width - 12, 220) : Math.min(340, this.width - 40);
        winH = small ? Math.max(this.height - 12, 220) : Math.min(420, this.height - 40);
        rowH = small ? 44 : 40;
        headerH = 36;
        winX = (this.width - winW) / 2;
        winY = (this.height - winH) / 2;
    }

    private void fillRound(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private boolean isSlider(String key) {
        return switch (key) {
            case "aimRange", "aimFov", "aimSmooth", "auraRange", "reachDistance",
                 "hitboxExpand", "missChance", "velHorizontal",
                 "potSlotMin", "potSlotMax" -> true;
            default -> false;
        };
    }

    private double minOf(String key) {
        return switch (key) {
            case "aimRange" -> 3.0;
            case "aimFov" -> 40;
            case "aimSmooth" -> 0.08;
            case "auraRange" -> 3.0;
            case "reachDistance" -> 3.0;
            case "hitboxExpand" -> 0.0;
            case "missChance" -> 0;
            case "velHorizontal" -> 0.40;
            case "potSlotMin", "potSlotMax" -> 0;
            default -> 0;
        };
    }

    private double maxOf(String key) {
        return switch (key) {
            case "aimRange" -> 6.0;
            case "aimFov" -> 180;
            case "aimSmooth" -> 0.55;
            case "auraRange" -> 4.5;
            case "reachDistance" -> 3.45;
            case "hitboxExpand" -> 0.40;
            case "missChance" -> 20;
            case "velHorizontal" -> 0.90;
            case "potSlotMin", "potSlotMax" -> 8;
            default -> 1;
        };
    }

    private double stepOf(String key) {
        return switch (key) {
            case "aimRange" -> 0.25;
            case "aimFov" -> 5;
            case "aimSmooth" -> 0.02;
            case "auraRange" -> 0.1;
            case "reachDistance" -> 0.05;
            case "hitboxExpand" -> 0.02;
            case "missChance" -> 1;
            case "velHorizontal" -> 0.05;
            case "potSlotMin", "potSlotMax" -> 1;
            default -> 0.1;
        };
    }

    private double getNum(String key) {
        return switch (key) {
            case "aimRange" -> ClientSettings.aimRange;
            case "aimFov" -> ClientSettings.aimFov;
            case "aimSmooth" -> ClientSettings.aimSmooth;
            case "auraRange" -> ClientSettings.auraRange;
            case "reachDistance" -> ClientSettings.reachDistance;
            case "hitboxExpand" -> ClientSettings.hitboxExpand;
            case "missChance" -> ClientSettings.missChance;
            case "velHorizontal" -> ClientSettings.velocityHorizontal;
            case "potSlotMin" -> ClientSettings.potSlotMin;
            case "potSlotMax" -> ClientSettings.potSlotMax;
            default -> 0;
        };
    }

    private void setNum(String key, double v) {
        v = Math.max(minOf(key), Math.min(maxOf(key), v));
        switch (key) {
            case "aimRange" -> ClientSettings.aimRange = v;
            case "aimFov" -> ClientSettings.aimFov = (float) v;
            case "aimSmooth" -> ClientSettings.aimSmooth = (float) v;
            case "auraRange" -> ClientSettings.auraRange = v;
            case "reachDistance" -> Reach.setReach(v);
            case "hitboxExpand" -> Hitboxes.setExpand(v);
            case "missChance" -> ClientSettings.missChance = (int) Math.round(v);
            case "velHorizontal" -> ClientSettings.velocityHorizontal = v;
            case "potSlotMin" -> ClientSettings.potSlotMin = (int) Math.round(v);
            case "potSlotMax" -> ClientSettings.potSlotMax = (int) Math.round(v);
            default -> {}
        }
    }

    private String labelOf(String key) {
        return switch (key) {
            case "aimMode" -> "Aim mode";
            case "targetPriority" -> "Target priority";
            case "velocityMode" -> "Velocity mode";
            case "aimRange" -> "Aim range";
            case "aimFov" -> "Aim FOV";
            case "aimSmooth" -> "Aim smooth";
            case "auraRange" -> "KillAura range";
            case "reachDistance" -> "Reach distance";
            case "hitboxExpand" -> "Hitbox expand";
            case "missChance" -> "Miss chance";
            case "velHorizontal" -> "Velocity horizontal";
            case "potSlotMin" -> "Pot slot min";
            case "potSlotMax" -> "Pot slot max";
            case "requireAttackKey" -> "Require attack key";
            case "Favorite" -> "Favorite module";
            case "Save & Back" -> "Save & Back";
            default -> key;
        };
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
            case "reachDistance" -> String.format("%.2f", ClientSettings.reachDistance);
            case "hitboxExpand" -> String.format("%.2f", ClientSettings.hitboxExpand);
            case "missChance" -> String.valueOf(ClientSettings.missChance);
            case "velHorizontal" -> String.format("%.2f", ClientSettings.velocityHorizontal);
            case "potSlotMin" -> String.valueOf(ClientSettings.potSlotMin);
            case "potSlotMax" -> String.valueOf(ClientSettings.potSlotMax);
            case "requireAttackKey" -> ClientSettings.requireAttackKey ? "on" : "off";
            case "Favorite" ->
                    module != null && ClientSettings.isFavorite(module.getName()) ? "yes" : "no";
            case "Save & Back" -> "";
            default -> "?";
        };
    }

    private void cycle(String key) {
        switch (key) {
            case "aimMode" ->
                    ClientSettings.aimMode =
                            "silent".equalsIgnoreCase(ClientSettings.aimMode) ? "classic" : "silent";
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
            case "requireAttackKey" -> ClientSettings.requireAttackKey = !ClientSettings.requireAttackKey;
            case "Favorite" -> {
                if (module != null) ClientSettings.toggleFavorite(module.getName());
            }
            case "Save & Back" -> {
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                close();
                return;
            }
            default -> {
                if (isSlider(key)) {
                    double n = getNum(key) + stepOf(key);
                    if (n > maxOf(key) + 1e-6) n = minOf(key);
                    setNum(key, n);
                }
            }
        }
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
    }

    private void applySliderDrag(String key, double mouseX, int trackX, int trackW) {
        double t = (mouseX - trackX) / (double) Math.max(1, trackW);
        t = Math.max(0, Math.min(1, t));
        double v = minOf(key) + t * (maxOf(key) - minOf(key));
        double step = stepOf(key);
        v = Math.round(v / step) * step;
        setNum(key, v);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        computeLayout();
        int x = winX;
        int y = winY;

        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);
        ctx.fill(x + 3, y + 4, x + winW + 3, y + winH + 4, 0x44000000);
        fillRound(ctx, x, y, x + winW, y + winH, BG_WINDOW);

        String title = module != null ? module.getName() : "Settings";
        ctx.drawTextWithShadow(textRenderer, "§b← §f" + title, x + 12, y + (headerH / 2) - 4, TEXT);

        int closeX = x + winW - 22;
        int closeY = y + (headerH / 2) - 6;
        boolean hoverClose = mouseX >= closeX - 4 && mouseX <= closeX + 12
                && mouseY >= closeY - 2 && mouseY <= closeY + 12;
        ctx.drawTextWithShadow(textRenderer, "×", closeX, closeY, hoverClose ? 0xFFFF8888 : TEXT_DIM);

        int listTop = y + headerH + 4;
        int listBottom = y + winH - 12;
        int maxRows = Math.max(1, (listBottom - listTop) / rowH);

        for (int i = 0; i < ROWS.length; i++) {
            if (i < scroll) continue;
            int di = i - scroll;
            if (di >= maxRows) break;

            String key = ROWS[i];
            int ry = listTop + di * rowH;
            boolean hover = mouseX >= x + 8 && mouseX < x + winW - 8 && mouseY >= ry && mouseY < ry + rowH;
            boolean saveRow = key.equals("Save & Back");

            if (hover || saveRow) {
                fillRound(ctx, x + 8, ry + 1, x + winW - 8, ry + rowH - 2,
                        saveRow ? 0xFF122018 : BG_ROW);
            }

            String label = labelOf(key);
            String val = valueOf(key);

            if (saveRow) {
                int tw = textRenderer.getWidth("Save & Back");
                ctx.drawTextWithShadow(textRenderer, "§aSave & Back",
                        x + (winW - tw) / 2, ry + rowH / 2 - 4, TEXT);
            } else if (isSlider(key)) {
                ctx.drawTextWithShadow(textRenderer, label, x + 16, ry + 8, TEXT);
                ctx.drawTextWithShadow(textRenderer, val, x + winW - 16 - textRenderer.getWidth(val),
                        ry + 8, ACCENT);
                int trackX = x + 16;
                int trackW = winW - 32;
                int trackY = ry + rowH - 14;
                ctx.fill(trackX, trackY, trackX + trackW, trackY + 4, TRACK);
                double t = (getNum(key) - minOf(key)) / (maxOf(key) - minOf(key));
                t = Math.max(0, Math.min(1, t));
                int fillW = (int) (trackW * t);
                ctx.fill(trackX, trackY, trackX + fillW, trackY + 4, ACCENT);
                int knobX = trackX + fillW - 3;
                ctx.fill(knobX, trackY - 3, knobX + 6, trackY + 7, 0xFFFFFFFF);
            } else {
                ctx.drawTextWithShadow(textRenderer, label, x + 16, ry + rowH / 2 - 4, TEXT);
                int vw = textRenderer.getWidth(val);
                int px = x + winW - 16 - vw - 10;
                int py = ry + rowH / 2 - 7;
                fillRound(ctx, px, py, x + winW - 12, py + 14, 0xFF1A2A32);
                ctx.drawTextWithShadow(textRenderer, val, px + 5, py + 3, ACCENT);
            }
            ctx.fill(x + 16, ry + rowH - 1, x + winW - 16, ry + rowH, DIVIDER);
        }

        ctx.drawTextWithShadow(textRenderer, "§8Click cycle · drag sliders · auto-saves",
                x + 12, y + winH - 11, 0xFF555566);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        computeLayout();
        double mx = click.x(), my = click.y();
        int x = winX, y = winY;
        int closeX = x + winW - 22;
        int closeY = y + (headerH / 2) - 6;
        if (my >= y && my <= y + headerH) {
            if (mx >= x && mx <= x + 80) { close(); return true; }
            if (mx >= closeX - 4 && mx <= closeX + 12) { close(); return true; }
        }
        int listTop = y + headerH + 4;
        int listBottom = y + winH - 12;
        int maxRows = Math.max(1, (listBottom - listTop) / rowH);
        for (int i = 0; i < ROWS.length; i++) {
            if (i < scroll) continue;
            int di = i - scroll;
            if (di >= maxRows) break;
            int ry = listTop + di * rowH;
            if (mx >= x + 8 && mx <= x + winW - 8 && my >= ry && my < ry + rowH) {
                String key = ROWS[i];
                if (isSlider(key)) {
                    int trackY = ry + rowH - 14;
                    if (my >= trackY - 6 && my <= trackY + 10) {
                        dragging = i;
                        applySliderDrag(key, mx, x + 16, winW - 32);
                        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                        return true;
                    }
                }
                cycle(key);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (dragging >= 0 && dragging < ROWS.length) {
            computeLayout();
            String key = ROWS[dragging];
            if (isSlider(key)) {
                applySliderDrag(key, click.x(), winX + 16, winW - 32);
                return true;
            }
        }
        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (dragging >= 0) {
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            dragging = -1;
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double h, double v) {
        computeLayout();
        int maxRows = Math.max(1, (winH - headerH - 16) / rowH);
        int maxScroll = Math.max(0, ROWS.length - maxRows);
        if (v > 0) scroll = Math.max(0, scroll - 1);
        else if (v < 0) scroll = Math.min(maxScroll, scroll + 1);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
