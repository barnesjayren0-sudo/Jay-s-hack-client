package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.BaseFinder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Vector4f;

/**
 * Screen-space ESP markers for BaseFinder hits.
 * Uses Hud overlay projection — no WorldRenderEvents (removed/moved on 1.21.11 Fabric).
 */
public final class WorldEspRenderer {

    private static boolean registered;

    private WorldEspRenderer() {}

    public static void register() {
        if (registered) return;
        registered = true;
        // Drawn from HudRenderer.drawWorldEsp() each frame — no world render API needed.
    }

    /** Call from HudRenderer while HUD is active. */
    public static void drawHudOverlay(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;
        if (JayHackClient.moduleManager == null) return;

        Module bf = JayHackClient.moduleManager.getModuleByName("BaseFinder");
        Module se = JayHackClient.moduleManager.getModuleByName("StorageESP");
        Module ff = JayHackClient.moduleManager.getModuleByName("FarmFinder");
        boolean baseOn = bf != null && bf.isEnabled();
        boolean storageOn = se != null && se.isEnabled();
        boolean farmOn = ff != null && ff.isEnabled();
        if (!baseOn && !storageOn && !farmOn) return;
        if (BaseFinder.lastHits.isEmpty()) return;

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d camPos = cam.getPos();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Approximate FOV projection (good enough for markers on mobile)
        float yaw = cam.getYaw();
        float pitch = cam.getPitch();
        float fov = 70f;
        try {
            fov = (float) mc.options.getFov().getValue();
        } catch (Throwable ignored) {}

        int drawn = 0;
        int maxDraw = Math.min(40, BaseFinder.maxEsp == 0 ? 40 : BaseFinder.maxEsp);

        for (BaseFinder.Hit hit : BaseFinder.lastHits) {
            if (drawn >= maxDraw) break;

            if (!baseOn) {
                if (storageOn && farmOn) {
                    if (!isStorageLabel(hit.label) && !isFarmLabel(hit.label)) continue;
                } else if (storageOn && !isStorageLabel(hit.label)) continue;
                else if (farmOn && !isFarmLabel(hit.label)) continue;
            }

            BlockPos p = hit.pos;
            double wx = p.getX() + 0.5 - camPos.x;
            double wy = p.getY() + 0.5 - camPos.y;
            double wz = p.getZ() + 0.5 - camPos.z;
            double dist = Math.sqrt(wx * wx + wy * wy + wz * wz);
            if (dist > BaseFinder.espRange || dist < 0.5) continue;

            // Rotate into view space
            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);
            double cosY = Math.cos(-yawRad);
            double sinY = Math.sin(-yawRad);
            double cosP = Math.cos(-pitchRad);
            double sinP = Math.sin(-pitchRad);

            double x1 = wx * cosY - wz * sinY;
            double z1 = wx * sinY + wz * cosY;
            double y1 = wy * cosP - z1 * sinP;
            double z2 = wy * sinP + z1 * cosP;

            if (z2 <= 0.1) continue; // behind camera

            double scale = (sh / 2.0) / Math.tan(Math.toRadians(fov / 2.0));
            int sx = (int) (sw / 2.0 + (x1 / z2) * scale);
            int sy = (int) (sh / 2.0 - (y1 / z2) * scale);

            if (sx < 2 || sx > sw - 2 || sy < 2 || sy > sh - 2) continue;

            int color = hit.color | 0xFF000000;
            int size = Math.max(2, (int) (6 - dist / 20));

            // Marker box
            ctx.fill(sx - size, sy - size, sx + size, sy + size, color);
            // Outline
            ctx.fill(sx - size - 1, sy - size - 1, sx + size + 1, sy - size, 0xAA000000);
            ctx.fill(sx - size - 1, sy + size, sx + size + 1, sy + size + 1, 0xAA000000);

            if (dist < 48 && BaseFinder.drawTracers) {
                // Tiny label
                String lab = hit.label.length() > 8 ? hit.label.substring(0, 8) : hit.label;
                ctx.drawTextWithShadow(mc.textRenderer, lab, sx + size + 2, sy - 4, color);
            }

            drawn++;
        }
    }

    private static boolean isStorageLabel(String label) {
        String l = label.toLowerCase();
        return l.contains("chest") || l.contains("barrel") || l.contains("shulker")
                || l.contains("hopper") || l.contains("ender") || l.contains("dispenser")
                || l.contains("dropper");
    }

    private static boolean isFarmLabel(String label) {
        String l = label.toLowerCase();
        return l.contains("farm") || l.contains("kelp") || l.contains("cane")
                || l.contains("bamboo") || l.contains("wart") || l.contains("crop")
                || l.contains("melon") || l.contains("farmland") || l.contains("compost")
                || l.contains("cactus") || l.contains("cocoa") || l.contains("berry");
    }
}
