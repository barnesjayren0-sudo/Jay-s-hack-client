package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Computes aim angles without applying them to the camera every tick.
 * applyServerView() briefly sets rotation for the attack, then restores — silent-style.
 */
public final class SilentRotations {

    private SilentRotations() {}

    public static float[] anglesTo(Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return null;

        Vec3d eyes = mc.player.getEyePos();
        double body = 0.82 + MathUtil.randomDouble(-0.05, 0.05);
        Vec3d pos = target.getEntityPos().add(
                Humanizer.aimJitter() * 0.015,
                target.getHeight() * body,
                Humanizer.aimJitter() * 0.015
        );

        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        pitch = MathHelper.clamp(pitch, -90f, 90f);
        return new float[]{yaw, pitch};
    }

    /** True if player is already roughly looking at target (FOV check). */
    public static boolean inFov(Entity target, float fovDeg) {
        float[] ang = anglesTo(target);
        if (ang == null) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        float dyaw = MathHelper.wrapDegrees(ang[0] - mc.player.getYaw());
        float dpitch = ang[1] - mc.player.getPitch();
        return Math.abs(dyaw) <= fovDeg && Math.abs(dpitch) <= fovDeg;
    }

    /**
     * Silent-style: rotate for one attack, swing, restore camera.
     * Less obvious than continuous AimAssist lock.
     */
    public static void silentLookForHit(Entity target, Runnable attack) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return;

        float[] ang = anglesTo(target);
        if (ang == null) return;

        float oldYaw = mc.player.getYaw();
        float oldPitch = mc.player.getPitch();

        mc.player.setYaw(ang[0]);
        mc.player.setPitch(ang[1]);

        attack.run();

        // restore so spectators / client view don't see constant snap
        mc.player.setYaw(oldYaw);
        mc.player.setPitch(oldPitch);
    }
}
