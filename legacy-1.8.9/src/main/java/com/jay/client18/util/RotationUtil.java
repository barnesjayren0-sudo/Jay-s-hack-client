package com.jay.client18.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public final class RotationUtil {

    private RotationUtil() {}

    public static float[] anglesTo(Entity e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || e == null) return null;
        double dx = e.posX - mc.thePlayer.posX;
        double dy = (e.posY + e.getEyeHeight() * 0.85) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = e.posZ - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(dy, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    /** Soft look with small jitter — never snaps. */
    public static void softLook(Entity e, float strength) {
        Minecraft mc = Minecraft.getMinecraft();
        float[] a = anglesTo(e);
        if (a == null || mc.thePlayer == null) return;

        float strength2 = Humanizer.soft(strength);
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        float dy = MathHelper.wrapAngleTo180_float(a[0] - yaw);
        float dp = a[1] - pitch;

        // Cap max step per tick so it doesn't look like silent snap
        if (dy > 8f) dy = 8f;
        if (dy < -8f) dy = -8f;
        if (dp > 5f) dp = 5f;
        if (dp < -5f) dp = -5f;

        dy += Humanizer.aimJitter();
        dp += Humanizer.aimJitter() * 0.5f;

        mc.thePlayer.rotationYaw = yaw + dy * strength2;
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(pitch + dp * strength2, -90f, 90f);
    }

    public static float yawDiff(Entity e) {
        Minecraft mc = Minecraft.getMinecraft();
        float[] a = anglesTo(e);
        if (a == null || mc.thePlayer == null) return 999f;
        return Math.abs(MathHelper.wrapAngleTo180_float(a[0] - mc.thePlayer.rotationYaw));
    }
}
