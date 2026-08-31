package com.jay.hackclient.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Stable world → screen projection for HUD ESP / nametags.
 * Uses Camera rotation (fixes left/right inversion from manual yaw math).
 */
public final class RenderUtil {

    private RenderUtil() {}

    private static Vec3d cameraPos(Camera cam) {
        // 1.21.x Yarn: getCameraPos(); some forks use getPos()
        try {
            return cam.getCameraPos();
        } catch (Throwable ignored) {}
        try {
            return (Vec3d) Camera.class.getMethod("getPos").invoke(cam);
        } catch (Throwable ignored) {}
        try {
            return (Vec3d) Camera.class.getMethod("getCameraPos").invoke(cam);
        } catch (Throwable ignored) {}
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc != null && mc.player != null) {
            return mc.player.getEyePos();
        }
        return Vec3d.ZERO;
    }

    /**
     * @return int[]{sx, sy} or null if behind camera / invalid
     */
    public static int[] worldToScreen(double wx, double wy, double wz) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.gameRenderer == null || mc.getWindow() == null) return null;

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d camPos = cameraPos(cam);

        double dx = wx - camPos.x;
        double dy = wy - camPos.y;
        double dz = wz - camPos.z;

        Vector3f v = new Vector3f((float) dx, (float) dy, (float) dz);
        Quaternionf rot = new Quaternionf(cam.getRotation());
        rot.conjugate();
        v.rotate(rot);

        // Forward is -Z in camera space after conjugate
        float depth = -v.z;
        if (depth < 0.05f) return null;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        double fovDeg = 70.0;
        try {
            Object fovObj = mc.options.getFov().getValue();
            if (fovObj instanceof Number n) fovDeg = n.doubleValue();
        } catch (Throwable ignored) {}

        double fovRad = Math.toRadians(fovDeg);
        double scale = (sh * 0.5) / Math.tan(fovRad * 0.5);

        int sx = (int) Math.round(sw * 0.5 + (v.x / depth) * scale);
        int sy = (int) Math.round(sh * 0.5 - (v.y / depth) * scale);

        if (sx < -sw || sx > sw * 2 || sy < -sh || sy > sh * 2) return null;
        return new int[]{sx, sy};
    }

    public static int[] worldToScreen(Vec3d pos) {
        if (pos == null) return null;
        return worldToScreen(pos.x, pos.y, pos.z);
    }
}
