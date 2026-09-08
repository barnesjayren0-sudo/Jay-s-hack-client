package com.jay.hackclient.util;

import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Central rotation pipeline (LiquidBounce-inspired):
 * 1) claim owner
 * 2) compute angles
 * 3) apply soft step + deadzone + max turn
 * Avoids dual modules fighting the camera.
 */
public final class RotationSystem {

    private RotationSystem() {}

    public static boolean lookAt(
            String module,
            int priority,
            Entity target,
            float smoothness,
            int holdMs
    ) {
        if (target == null) return false;
        if (!RotationOwner.tryClaim(module, priority, holdMs)) return false;
        RotationUtil.lookAt(target, smoothness);
        return true;
    }

    public static float[] anglesTo(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return null;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = target.getEntityPos().add(0.0, target.getHeight() * 0.72, 0.0);

        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0E-4) return null;

        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        pitch = MathHelper.clamp(pitch, -89f, 89f);
        return new float[]{yaw, pitch};
    }

    /** Degrees from crosshair to entity. */
    public static float angleTo(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float[] a = anglesTo(target);
        if (a == null || mc.player == null) return 999f;
        float dy = MathHelper.wrapDegrees(a[0] - mc.player.getYaw());
        float dp = a[1] - mc.player.getPitch();
        return (float) Math.sqrt(dy * dy + dp * dp);
    }

    public static boolean inFov(Entity target, float fov) {
        return angleTo(target) <= fov * 0.5f;
    }

    public static void applyGhostCaps() {
        // Ensure settings never go blatant by accident
        if (ClientSettings.aimMaxStep > 5.5f) ClientSettings.aimMaxStep = 5.5f;
        if (ClientSettings.aimSmooth > 0.35f) ClientSettings.aimSmooth = 0.35f;
        if (ClientSettings.auraRange > 3.35) ClientSettings.auraRange = 3.35;
        if (ClientSettings.reachDistance > 3.3) ClientSettings.reachDistance = 3.3;
    }
}
