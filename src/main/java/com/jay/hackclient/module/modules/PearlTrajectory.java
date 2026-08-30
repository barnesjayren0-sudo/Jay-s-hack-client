package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Simple ender pearl arc preview on HUD. */
public class PearlTrajectory extends Module {

    public final NumberSetting steps = new NumberSetting("Steps", "Sim steps", 40, 20, 80, 5);

    public PearlTrajectory() {
        super("PearlTrajectory", "Pearl throw arc preview", Category.RENDER);
        addSetting(steps);
    }

    public static void draw(DrawContext ctx) {
        if (JayHackClient.moduleManager == null) return;
        Module m = JayHackClient.moduleManager.getModuleByName("PearlTrajectory");
        if (!(m instanceof PearlTrajectory pt) || !m.isEnabled()) return;
        if (pt.mc.player == null || pt.mc.world == null || pt.mc.gameRenderer == null) return;
        if (!pt.mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)
                && !pt.mc.player.getOffHandStack().isOf(Items.ENDER_PEARL)) return;

        Camera cam = pt.mc.gameRenderer.getCamera();
        Vec3d eye = pt.mc.player.getEyePos();
        float yaw = pt.mc.player.getYaw();
        float pitch = pt.mc.player.getPitch();
        double radY = Math.toRadians(yaw);
        double radP = Math.toRadians(pitch);

        Vec3d vel = new Vec3d(
                -Math.sin(radY) * Math.cos(radP),
                -Math.sin(radP),
                Math.cos(radY) * Math.cos(radP)
        ).multiply(1.5);

        int sw = pt.mc.getWindow().getScaledWidth();
        int sh = pt.mc.getWindow().getScaledHeight();
        float fov = 70f;
        try { fov = (float) pt.mc.options.getFov().getValue(); } catch (Throwable ignored) {}
        double scale = (sh / 2.0) / Math.tan(Math.toRadians(fov / 2.0));
        var camPos = cam.getCameraPos();

        Vec3d pos = eye;
        int prevSx = -1, prevSy = -1;
        int max = pt.steps.getInt();
        for (int i = 0; i < max; i++) {
            pos = pos.add(vel);
            vel = vel.multiply(0.99).add(0, -0.03, 0);

            BlockPos bp = BlockPos.ofFloored(pos);
            if (!pt.mc.world.getBlockState(bp).isAir()) break;

            double dx = pos.x - camPos.x, dy = pos.y - camPos.y, dz = pos.z - camPos.z;
            double yawRad = Math.toRadians(cam.getYaw());
            double pitchRad = Math.toRadians(cam.getPitch());
            double cosY = Math.cos(-yawRad), sinY = Math.sin(-yawRad);
            double cosP = Math.cos(-pitchRad), sinP = Math.sin(-pitchRad);
            double x1 = dx * cosY - dz * sinY;
            double z1 = dx * sinY + dz * cosY;
            double y1 = dy * cosP - z1 * sinP;
            double z2 = dy * sinP + z1 * cosP;
            if (z2 <= 0.1) continue;
            int sx = (int) (sw / 2.0 + (x1 / z2) * scale);
            int sy = (int) (sh / 2.0 - (y1 / z2) * scale);
            if (prevSx >= 0) {
                ctx.fill(sx - 1, sy - 1, sx + 1, sy + 1, 0xFF55FFFF);
            }
            prevSx = sx;
            prevSy = sy;
        }
        if (prevSx >= 0) {
            ctx.fill(prevSx - 3, prevSy - 3, prevSx + 3, prevSy + 3, 0xFFFF5555);
        }
    }
}
