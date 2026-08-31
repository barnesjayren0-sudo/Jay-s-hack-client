package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Shows last / current reach distance. */
public class ReachHUD extends Module {

    private static double lastReach = 0;

    public ReachHUD() {
        super("ReachHUD", "Shows hit distance", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY
                && mc.crosshairTarget instanceof EntityHitResult ehr) {
            double dist = mc.player.getEyePos().distanceTo(ehr.getPos());
            if (dist > 0 && dist < 8) lastReach = dist;
        }
    }

    /** Called from KillAura / TriggerBot when a hit lands. */
    public static void recordHit(double dist) {
        if (dist > 0 && dist < 16) lastReach = dist;
    }

    public static void recordHit(float dist) {
        recordHit((double) dist);
    }

    public static double lastHitDist() {
        return lastReach;
    }

    public static void draw(DrawContext ctx) {
        if (ctx == null) return;
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null || client.player == null || client.textRenderer == null) return;
        Module self = null;
        try {
            if (com.jay.hackclient.JayHackClient.moduleManager != null) {
                self = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("ReachHUD");
            }
        } catch (Throwable ignored) {}
        if (self == null || !self.isEnabled()) return;

        String t = String.format("Reach §f%.2f", lastReach);
        int x = 4;
        int y = client.getWindow().getScaledHeight() - 28;
        ctx.drawTextWithShadow(client.textRenderer, t, x, y, 0xFF3DDCFF);
    }
}
