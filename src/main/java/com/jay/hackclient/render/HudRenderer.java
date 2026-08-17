package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HudRenderer {

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
        int y = 6;

        // Watermark top-left
        String water = "Jay §b" + JayHackClient.VERSION;
        context.fill(4, 4, 4 + mc.textRenderer.getWidth(water) + 10, 16, 0x66000000);
        context.drawTextWithShadow(mc.textRenderer, "§fJay §b" + JayHackClient.VERSION, 8, 6, 0xFFFFFF);

        // ArrayList top-right
        y = 6;
        for (Module m : enabled) {
            String label = m.getName();
            int w = mc.textRenderer.getWidth(label);
            int x = screenW - w - 10;
            int color = colorFor(m.getCategory());

            context.fill(x - 4, y - 1, screenW - 2, y + 10, 0x66000000);
            context.fill(screenW - 2, y - 1, screenW, y + 10, color | 0xFF000000);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, color);
            y += 11;
        }

        // Info bottom-left
        String info = String.format("§7FPS §f%d §8| §7XYZ §f%d %d %d",
                mc.getCurrentFps(),
                (int) mc.player.getX(),
                (int) mc.player.getY(),
                (int) mc.player.getZ());
        context.drawTextWithShadow(mc.textRenderer, info, 6, mc.getWindow().getScaledHeight() - 14, 0xFFFFFF);
    }

    private static int colorFor(Module.Category c) {
        return switch (c) {
            case COMBAT -> 0xFF5555;
            case MOVEMENT -> 0x55FF55;
            case RENDER -> 0x55FFFF;
            case PLAYER -> 0xFFAA00;
            case MISC -> 0xFF55FF;
            case WORLD -> 0xAAAAFF;
        };
    }
}
