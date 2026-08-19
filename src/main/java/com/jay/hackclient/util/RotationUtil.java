package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {

    private RotationUtil() {}

    /**
     * Smooth classic aim without per-tick jitter (that caused the wiggle).
     * Deadzone: if already close enough to target angles, do nothing.
     */
    public static void lookAt(Entity target, float smoothness) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return;

        Vec3d eyes = mc.player.getEyePos();
        // Stable body aim — no random offset each tick
        Vec3d pos = target.getEntityPos().add(0.0, target.getHeight() * 0.75, 0.0);

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

        // Deadzone — stop micro-correcting (main stutter source)
        if (Math.abs(yawDiff) < 1.8f && Math.abs(pitchDiff) < 1.5f) return;

        // Cap how much we can turn per tick so it never snaps/fights hard
        float maxStep = 4.5f;
        yawDiff = MathHelper.clamp(yawDiff, -maxStep, maxStep);
        pitchDiff = MathHelper.clamp(pitchDiff, -maxStep * 0.7f, maxStep * 0.7f);

        float t = MathHelper.clamp(smoothness, 0.08f, 0.45f); // hard cap — never aggressive

        mc.player.setYaw(mc.player.getYaw() + yawDiff * t);
        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + pitchDiff * t, -90f, 90f));
    }
}
