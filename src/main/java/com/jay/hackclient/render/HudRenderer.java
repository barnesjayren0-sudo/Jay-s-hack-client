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

        List<Module> enabled = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.isEnabled() && !m.getName().equalsIgnoreCase("HUD")) {
                enabled.add(m);
            }
        }
        enabled.sort(Comparator.comparingInt((Module m) -> -mc.textRenderer.getWidth(m.getName())));

        int x = 4;
        int y = 4;

        context.drawTextWithShadow(mc.textRenderer,
                "§bJay§f §7v" + JayHackClient.VERSION, x, y, 0xFFFFFF);
        y += 12;

        for (Module m : enabled) {
            context.drawTextWithShadow(mc.textRenderer, "§7> §f" + m.getName(), x, y, 0xAAAAAA);
            y += 10;
        }
    }
}
