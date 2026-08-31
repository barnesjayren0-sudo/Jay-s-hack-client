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

    /**
 * @return int[]{sx, sy} or null if behind camera / off far plane
 */
    public static int[] worldToScreen(double wx, double wy, double wz) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.gameRenderer == null || mc.getWindow() == null) return null;

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d camPos;
        try {
            camPos = cam.getPos();
        } catch (Throwable t) {
            try {
                // older mapping alias
                camPos = (Vec3d) Camera.class.getMethod("getCameraPos").invoke(cam);
            } catch (Throwable t2) {
                return null;
            }
        }

        double dx = wx - camPos.x;
        double dy = wy - camPos.y;
        double dz = wz - camPos.z;

        // Camera space: rotate world offset by inverse camera rotation
        Vector3f v = new Vector3f((float) dx, (float) dy, (float) dz);
        Quaternionf rot = new Quaternionf(cam.getRotation());
        rot.conjugate();
        v.rotate(rot);

        // After conjugate, forward is -Z in camera space
        float depth = -v.z;
        if (depth < 0.05f) return null;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        double fovDeg = 70.0;
        try {
            fovDeg = mc.options.getFov().getValue().doubleValue();
        } catch (Throwable ignored) {}
        // Approximate FOV modifiers (sprint/effects) without full GameRenderer access
        try {
            float f = mc.player != null ? mc.gameRenderer.getFov(cam, mc.getRenderTickCounter().getTickProgress(false), true) : (float) fovDeg;
            if (f > 1 && f < 180) fovDeg = f;
        } catch (Throwable ignored) {}

        double fovRad = Math.toRadians(fovDeg);
        // Vertical FOV projection
        double scale = (sh * 0.5) / Math.tan(fovRad * 0.5);

        int sx = (int) Math.round(sw * 0.5 + (v.x / depth) * scale);
        int sy = (int) Math.round(sh * 0.5 - (v.y / depth) * scale);

        // Allow slight off-screen for partial boxes; reject far-off
        if (sx < -sw || sx > sw * 2 || sy < -sh || sy > sh * 2) return null;
        return new int[]{sx, sy};
    }

    public static int[] worldToScreen(Vec3d pos) {
        if (pos == null) return null;
        return worldToScreen(pos.x, pos.y, pos.z);
    }
}
