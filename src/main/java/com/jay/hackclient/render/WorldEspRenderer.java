package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.BaseFinder;
import com.jay.hackclient.module.modules.HoleESP;
import com.jay.hackclient.module.modules.LogoutSpots;
import com.jay.hackclient.module.modules.Nametags;
import com.jay.hackclient.module.modules.StorageESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Screen-space ESP markers for finders, storage, holes, logout spots + combat overlays. */
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
        Module lo = JayHackClient.moduleManager.getModuleByName("LogoutSpots");
        Module nt = JayHackClient.moduleManager.getModuleByName("Nametags");

        boolean baseOn = bf != null && bf.isEnabled();
        boolean storageOn = se != null && se.isEnabled();
        boolean farmOn = ff != null && ff.isEnabled();
        boolean holeOn = he != null && he.isEnabled();
        boolean logoutOn = lo != null && lo.isEnabled();

        List<BaseFinder.Hit> hits = new ArrayList<>();
        if (baseOn || farmOn) hits.addAll(BaseFinder.lastHits);
        if (storageOn) hits.addAll(StorageESP.hits);
        if (holeOn) hits.addAll(HoleESP.holes);
        if (logoutOn) hits.addAll(LogoutSpots.espHits);

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d camPos = cam.getCameraPos();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        float yaw = cam.getYaw();
        float pitch = cam.getPitch();
        float fov = 70f;
        try { fov = (float) mc.options.getFov().getValue(); } catch (Throwable ignored) {}

        int drawn = 0;
        int maxDraw = 60;

        for (BaseFinder.Hit hit : hits) {
            if (drawn >= maxDraw) break;
            boolean isHole = hit.label.contains("Hole");
            boolean isLogout = hit.label.startsWith("LO:");
            boolean isStorage = isStorageLabel(hit.label);

            if (isLogout && !logoutOn) continue;
            if (isHole && !holeOn) continue;
            if (isStorage && !storageOn && !baseOn) continue;
            if (!isHole && !isLogout && !isStorage && !baseOn && !farmOn) continue;

            BlockPos p = hit.pos;
            double wx = p.getX() + 0.5 - camPos.x;
            double wy = p.getY() + 0.5 - camPos.y;
            double wz = p.getZ() + 0.5 - camPos.z;
            double dist = Math.sqrt(wx * wx + wy * wy + wz * wz);
            double maxR = isHole ? 40 : (isLogout ? 128 : BaseFinder.espRange);
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
            int size = isLogout ? Math.max(4, (int) (10 - dist / 15))
                    : isHole ? Math.max(3, (int) (8 - dist / 8))
                    : Math.max(2, (int) (6 - dist / 20));

            ctx.fill(sx - size, sy - size, sx + size, sy + size, color);
            ctx.fill(sx - size - 1, sy - size - 1, sx + size + 1, sy - size, 0xAA000000);
            ctx.fill(sx - size - 1, sy + size, sx + size + 1, sy + size + 1, 0xAA000000);
            if (isLogout) {
                int b = size + 2;
                ctx.fill(sx - b, sy - b, sx - b + 2, sy - b + 6, color);
                ctx.fill(sx - b, sy - b, sx - b + 6, sy - b + 2, color);
                ctx.fill(sx + b - 2, sy - b, sx + b, sy - b + 6, color);
                ctx.fill(sx + b - 6, sy - b, sx + b, sy - b + 2, color);
            }
            if (dist < 64) {
                String lab = hit.label.length() > 12 ? hit.label.substring(0, 12) : hit.label;
                ctx.drawTextWithShadow(mc.textRenderer, lab, sx + size + 2, sy - 4, color);
            }
            drawn++;
        }

        if (nt != null && nt.isEnabled() && nt instanceof Nametags tags) {
            double max = tags.range.get();
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player || !p.isAlive()) continue;
                double dist = mc.player.distanceTo(p);
                if (dist > max) continue;

                double wx = p.getX() - camPos.x;
                double wy = p.getY() + p.getHeight() + 0.3 - camPos.y;
                double wz = p.getZ() - camPos.z;

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
                if (sx < 4 || sx > sw - 4 || sy < 4 || sy > sh - 4) continue;

                String label = Nametags.formatTag(p, mc.player);
                int tw = mc.textRenderer.getWidth(label.replaceAll("§.", ""));
                int color = tags.colorArgb();
                ctx.fill(sx - tw / 2 - 2, sy - 2, sx + tw / 2 + 2, sy + 10, 0x88000000);
                ctx.drawTextWithShadow(mc.textRenderer, label, sx - tw / 2, sy, color);
            }
        }

        try { com.jay.hackclient.module.modules.PlayerBoxes.draw(ctx); } catch (Throwable ignored) {}
        try { com.jay.hackclient.module.modules.PearlTrajectory.draw(ctx); } catch (Throwable ignored) {}
        try { com.jay.hackclient.module.modules.CombatHUD.draw(ctx); } catch (Throwable ignored) {}
    }

    private static boolean isStorageLabel(String label) {
        String l = label.toLowerCase();
        return l.contains("chest") || l.contains("barrel") || l.contains("shulker")
                || l.contains("hopper") || l.contains("ender") || l.contains("dispenser")
                || l.contains("dropper");
    }
}
