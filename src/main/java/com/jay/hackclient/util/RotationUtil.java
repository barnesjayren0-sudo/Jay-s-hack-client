package com.jay.hackclient.util;

import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {

    private RotationUtil() {}

    public static void lookAt(Entity target, float smoothness) {
        lookAt(target, smoothness, AngleSmooth.Mode.SIGMOID);
    }

    public static void lookAt(Entity target, float smoothness, AngleSmooth.Mode mode) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = target.getEntityPos().add(0.0, target.getHeight() * 0.72, 0.0);

        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0E-4) return;

        float targetYaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float targetPitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        targetPitch = MathHelper.clamp(targetPitch, -89f, 89f);

        float yawDiff = MathHelper.wrapDegrees(targetYaw - mc.player.getYaw());
        float pitchDiff = targetPitch - mc.player.getPitch();

        if (Math.abs(yawDiff) < ClientSettings.aimDeadzone
                && Math.abs(pitchDiff) < ClientSettings.aimDeadzone * 0.85f) {
            return;
        }

        float maxStep = MathHelper.clamp(ClientSettings.aimMaxStep, 2.0f, 5.5f);
        float t = MathHelper.clamp(smoothness, 0.08f, 0.35f);

        float[] next = AngleSmooth.stepTowards(
                mc.player.getYaw(), mc.player.getPitch(),
                targetYaw, targetPitch,
                t, maxStep, mode
        );

        mc.player.setYaw(next[0]);
        mc.player.setPitch(next[1]);
    }
}
