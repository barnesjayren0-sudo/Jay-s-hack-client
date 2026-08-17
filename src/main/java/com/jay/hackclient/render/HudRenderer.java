package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

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

        List<Module> enabled = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.isEnabled() && !m.getName().equalsIgnoreCase("HUD")) {
                enabled.add(m);
            }
        }
        enabled.sort(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())));

        int screenW = mc.getWindow().getScaledWidth();

        // Watermark
        String water = "Jay";
        context.fill(4, 4, 8 + mc.textRenderer.getWidth(water + " " + JayHackClient.VERSION) + 6, 16, 0x990A0A10);
        context.fill(4, 4, 6, 16, 0xFF000000 | ACCENT);
        context.drawTextWithShadow(mc.textRenderer, "§dJ§fay §8" + JayHackClient.VERSION, 10, 6, 0xFFFFFF);

        // ArrayList
        int y = 20;
        for (Module m : enabled) {
            String label = m.getName();
            int w = mc.textRenderer.getWidth(label);
            int x = screenW - w - 10;
            context.fill(x - 4, y - 1, screenW - 2, y + 10, 0x990A0A10);
            context.fill(screenW - 2, y - 1, screenW, y + 10, 0xFF000000 | ACCENT);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, 0xE8E8F0);
            y += 11;
        }

        String info = String.format("§7%d fps  §8|  §f%d §7%d §f%d",
                mc.getCurrentFps(),
                (int) mc.player.getX(),
                (int) mc.player.getY(),
                (int) mc.player.getZ());
        context.drawTextWithShadow(mc.textRenderer, info, 6, mc.getWindow().getScaledHeight() - 14, 0xFFFFFF);
    }
}
