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
        double dy = (e.posY + e.getEyeHeight() * 0.9) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dz = e.posZ - mc.thePlayer.posZ;
        double dist = MathHelper.sqrt_double(dx * dx + dz * dz);
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(dy, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static void softLook(Entity e, float strength) {
        Minecraft mc = Minecraft.getMinecraft();
        float[] a = anglesTo(e);
        if (a == null || mc.thePlayer == null) return;
        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;
        float dy = MathHelper.wrapAngleTo180_float(a[0] - yaw);
        float dp = a[1] - pitch;
        strength = Math.max(0.05f, Math.min(1.0f, strength));
        mc.thePlayer.rotationYaw = yaw + dy * strength;
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(pitch + dp * strength, -90f, 90f);
    }

    public static float yawDiff(Entity e) {
        Minecraft mc = Minecraft.getMinecraft();
        float[] a = anglesTo(e);
        if (a == null || mc.thePlayer == null) return 999f;
        return Math.abs(MathHelper.wrapAngleTo180_float(a[0] - mc.thePlayer.rotationYaw));
    }
}
