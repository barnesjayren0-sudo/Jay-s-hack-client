package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class RotationUtil {

    private RotationUtil() {}

    public static void lookAt(Entity target, float smoothness) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || target == null) return;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.9, 0);

        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        pitch = MathHelper.clamp(pitch, -90f, 90f);

        // smoothness 1 = instant, lower = slower turn (more legit)
        float t = MathHelper.clamp(smoothness, 0.15f, 1f);
        float newYaw = MathUtil.lerp(mc.player.getYaw(), yaw, t);
        float newPitch = MathUtil.lerp(mc.player.getPitch(), pitch, t);

        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
    }
}
