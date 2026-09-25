package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** TargetStrafe — Circle / Adaptive / Keep orbit. */
public class TargetStrafe extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Strafe style", "Circle", "Circle", "Adaptive", "Keep");
    public final NumberSetting range = new NumberSetting("Range", "Engage range", 3.5, 2.0, 6.0, 0.1);
    public final NumberSetting speed = new NumberSetting("Speed", "Strafe strength", 0.22, 0.08, 0.5, 0.01);
    public final NumberSetting distance = new NumberSetting("Distance", "Orbit distance", 2.5, 1.0, 4.5, 0.1);
    public final NumberSetting fov = new NumberSetting("FOV", "Facing FOV", 120, 40, 360, 5);
    public final BoolSetting jump = new BoolSetting("Jump", "Jump while strafing", false);
    public final BoolSetting onlyOnGround = new BoolSetting("On Ground", "Only grounded", false);

    private int dir = 1;

    public TargetStrafe() {
        super("TargetStrafe", "Circle / adaptive strafe around target", Category.COMBAT);
        addSetting(mode); addSetting(range); addSetting(speed); addSetting(distance);
        addSetting(fov); addSetting(jump); addSetting(onlyOnGround);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (onlyOnGround.get() && !mc.player.isOnGround()) return;

        PlayerEntity t = null;
        try { t = TargetUtil.find(range.get(), fov.getFloat()); } catch (Throwable ignored) {}
        if (t == null) {
            double best = range.get();
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player || !p.isAlive()) continue;
                try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
                double d = mc.player.distanceTo(p);
                if (d < best) { best = d; t = p; }
            }
        }
        if (t == null) { setTag(null); return; }
        setTag(t.getName().getString());
        if (mc.player.horizontalCollision) dir = -dir;

        double dx = t.getX() - mc.player.getX();
        double dz = t.getZ() - mc.player.getZ();
        double dist = Math.sqrt(dx*dx + dz*dz);
        double targetYaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;
        double sp = speed.get();
        Vec3d v = mc.player.getVelocity();

        switch (mode.get()) {
            case "Adaptive" -> {
                if (dist > distance.get() + 0.4) {
                    float rad = (float) Math.toRadians(targetYaw);
                    mc.player.setVelocity(v.x*0.5 + -MathHelper.sin(rad)*sp, v.y, v.z*0.5 + MathHelper.cos(rad)*sp);
                } else {
                    float rad = (float) Math.toRadians(targetYaw + 90*dir);
                    mc.player.setVelocity(v.x*0.55 + MathHelper.sin(-rad)*sp, v.y, v.z*0.55 + MathHelper.cos(rad)*sp);
                }
            }
            case "Keep" -> {
                float rad;
                if (dist > distance.get()) rad = (float) Math.toRadians(targetYaw);
                else if (dist < distance.get() - 0.3) rad = (float) Math.toRadians(targetYaw + 180);
                else rad = (float) Math.toRadians(targetYaw + 90*dir);
                mc.player.setVelocity(v.x*0.5 + -MathHelper.sin(rad)*sp, v.y, v.z*0.5 + MathHelper.cos(rad)*sp);
            }
            default -> {
                float rad = (float) Math.toRadians(targetYaw + 90*dir);
                mc.player.setVelocity(v.x*0.55 + MathHelper.sin(-rad)*sp, v.y, v.z*0.55 + MathHelper.cos(rad)*sp);
            }
        }
        if (jump.get() && mc.player.isOnGround()) mc.player.jump();
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
