package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.BaseFinder;
import com.jay.hackclient.module.modules.HoleESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen-space ESP markers for BaseFinder + HoleESP hits.
 */
public final class WorldEspRenderer {

    private static boolean registered;

    private WorldEspRenderer() {}

    public static void register() {
        if (registered) return;
        registered = true;
    }

    public static void drawHudOverlay(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) return;
        if (JayHackClient.moduleManager == null) return;

        Module bf = JayHackClient.moduleManager.getModuleByName("BaseFinder");
        Module se = JayHackClient.moduleManager.getModuleByName("StorageESP");
        Module ff = JayHackClient.moduleManager.getModuleByName("FarmFinder");
        Module he = JayHackClient.moduleManager.getModuleByName("HoleESP");
        boolean baseOn = bf != null && bf.isEnabled();
        boolean storageOn = se != null && se.isEnabled();
        boolean farmOn = ff != null && ff.isEnabled();
        boolean holeOn = he != null && he.isEnabled();

        List<BaseFinder.Hit> hits = new ArrayList<>();
        if (baseOn || storageOn || farmOn) {
            hits.addAll(BaseFinder.lastHits);
        }
        if (holeOn) {
            hits.addAll(HoleESP.holes);
        }
        if (hits.isEmpty()) return;

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d camPos = cam.getCameraPos();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        float yaw = cam.getYaw();
        float pitch = cam.getPitch();
        float fov = 70f;
        try {
            fov = (float) mc.options.getFov().getValue();
        } catch (Throwable ignored) {}

        int drawn = 0;
        int maxDraw = Math.min(50, BaseFinder.maxEsp == 0 ? 50 : BaseFinder.maxEsp);

        for (BaseFinder.Hit hit : hits) {
            if (drawn >= maxDraw) break;

            boolean isHole = hit.label.contains("Hole");
            if (!isHole && !baseOn) {
                if (storageOn && farmOn) {
                    if (!isStorageLabel(hit.label) && !isFarmLabel(hit.label)) continue;
                } else if (storageOn && !isStorageLabel(hit.label)) continue;
                else if (farmOn && !isFarmLabel(hit.label)) continue;
            }
            if (isHole && !holeOn) continue;

            BlockPos p = hit.pos;
            double wx = p.getX() + 0.5 - camPos.x;
            double wy = p.getY() + 0.5 - camPos.y;
            double wz = p.getZ() + 0.5 - camPos.z;
            double dist = Math.sqrt(wx * wx + wy * wy + wz * wz);
            double maxR = isHole ? 40 : BaseFinder.espRange;
            if (dist > maxR || dist < 0.4) continue;

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
            if (z2 <= 0.1) continue;

            double scale = (sh / 2.0) / Math.tan(Math.toRadians(fov / 2.0));
            int sx = (int) (sw / 2.0 + (x1 / z2) * scale);
            int sy = (int) (sh / 2.0 - (y1 / z2) * scale);
            if (sx < 2 || sx > sw - 2 || sy < 2 || sy > sh - 2) continue;

            int color = hit.color | 0xFF000000;
            int size = isHole ? Math.max(3, (int) (8 - dist / 8)) : Math.max(2, (int) (6 - dist / 20));

            ctx.fill(sx - size, sy - size, sx + size, sy + size, color);
            ctx.fill(sx - size - 1, sy - size - 1, sx + size + 1, sy - size, 0xAA000000);
            ctx.fill(sx - size - 1, sy + size, sx + size + 1, sy + size + 1, 0xAA000000);

            if (dist < 48) {
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
