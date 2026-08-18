package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.TargetHUD;
import com.jay.hackclient.util.Mobile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HudRenderer {

    private static final int ACCENT = 0xB24BF3;

    private HudRenderer() {}

    public static void render(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || JayHackClient.moduleManager == null) return;
        if (mc.options.hudHidden) return;

        boolean phone = Mobile.isSmallScreen();

        List<Module> enabled = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.isEnabled() && !m.getName().equalsIgnoreCase("HUD")) {
                enabled.add(m);
            }
        }
        enabled.sort(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())));

        // Cap arraylist length on phones
        int maxList = phone ? 8 : 24;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        String ver = JayHackClient.VERSION;
        int ww = mc.textRenderer.getWidth("Jay " + ver) + 14;
        context.fill(4, 4, 4 + ww, 17, 0x990A0A10);
        context.fill(4, 4, 6, 17, 0xFF000000 | ACCENT);
        context.drawTextWithShadow(mc.textRenderer, "§dJ§fay §8" + ver, 10, 7, 0xFFFFFF);

        int y = 22;
        int shown = 0;
        for (Module m : enabled) {
            if (shown >= maxList) break;
            String label = m.getName();
            int w = mc.textRenderer.getWidth(label);
            int x = screenW - w - 10;
            context.fill(x - 5, y - 1, screenW - 2, y + 10, 0x990A0A10);
            context.fill(screenW - 2, y - 1, screenW, y + 10, 0xFF000000 | ACCENT);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, 0xE8E8F0);
            y += 11;
            shown++;
        }

        Module th = JayHackClient.moduleManager.getModuleByName("TargetHUD");
        if (th != null && th.isEnabled() && TargetHUD.currentTarget != null) {
            drawTargetHud(context, mc, TargetHUD.currentTarget, screenW, screenH, phone);
        }

        if (!phone) {
            String info = String.format("§7%d fps §8| §f%d %d %d",
                    mc.getCurrentFps(),
                    (int) mc.player.getX(),
                    (int) mc.player.getY(),
                    (int) mc.player.getZ());
            context.drawTextWithShadow(mc.textRenderer, info, 6, screenH - 14, 0xFFFFFF);
        }
    }

    private static void drawTargetHud(DrawContext context, MinecraftClient mc, PlayerEntity target,
                                      int sw, int sh, boolean phone) {
        String name = target.getName().getString();
        float hp = target.getHealth() + target.getAbsorptionAmount();
        float max = Math.max(1f, target.getMaxHealth());
        float pct = Math.min(1f, hp / max);

        int boxW = phone ? 100 : 120;
        int boxH = 28;
        int bx = sw / 2 - boxW / 2;
        int by = sh / 2 + (phone ? 24 : 30);

        context.fill(bx, by, bx + boxW, by + boxH, 0xCC0A0A10);
        context.fill(bx, by, bx + 2, by + boxH, 0xFF000000 | ACCENT);
        context.drawTextWithShadow(mc.textRenderer, name, bx + 8, by + 4, 0xFFFFFF);

        int barX = bx + 8;
        int barY = by + 16;
        int barW = boxW - 16;
        context.fill(barX, barY, barX + barW, barY + 6, 0xFF222228);
        int fill = (int) (barW * pct);
        int hpColor = pct > 0.5f ? 0xFF44CC66 : (pct > 0.25f ? 0xFFCCAA33 : 0xFFCC4444);
        context.fill(barX, barY, barX + fill, barY + 6, hpColor);
    }
}
