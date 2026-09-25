package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.util.math.Vec3d;

/**
 * Speed — multi-mode (Orchard / Ghost style).
 * Vanilla, BHop, LowHop, YPort, Strafe, OnGround, Timer, Custom.
 * Real PlayerMoveC2SPacket only — NO custom packets.
 */
public class Speed extends Module {

    public final ModeSetting mode = new ModeSetting(
            "Mode", "Speed style", "BHop",
            "Vanilla", "BHop", "LowHop", "YPort", "Strafe", "OnGround", "Timer", "Custom"
    );
    public final NumberSetting speed = new NumberSetting(
            "Speed", "Horizontal speed factor", 1.25, 1.0, 3.0, 0.05
    );
    public final NumberSetting hopHeight = new NumberSetting(
            "Hop Height", "Jump velocity for hop modes", 0.42, 0.1, 0.7, 0.01
    );
    public final NumberSetting timerSpeed = new NumberSetting(
            "Timer", "Timer multiplier (Timer mode)", 1.15, 1.0, 2.0, 0.05
    );
    public final NumberSetting friction = new NumberSetting(
            "Friction", "Ground friction (lower = more slide)", 0.6, 0.2, 1.0, 0.05
    );
    public final NumberSetting airSpeed = new NumberSetting(
            "Air Speed", "Air acceleration (Strafe/BHop)", 0.03, 0.01, 0.12, 0.005
    );
    public final BoolSetting autoJump = new BoolSetting(
            "Auto Jump", "Jump automatically when moving", true
    );
    public final BoolSetting pauseInLiquid = new BoolSetting(
            "Pause Liquid", "Disable in water/lava", true
    );
    public final BoolSetting pauseSneak = new BoolSetting(
            "Pause Sneak", "Disable while sneaking", true
    );
    public final BoolSetting realPackets = new BoolSetting(
            "Real Packets", "Sync position with vanilla move packets", true
    );

    private int groundTicks;
    private int airTicks;

    public Speed() {
        super("Speed", "Multi-mode speed (BHop, LowHop, YPort, Strafe…)", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
        addSetting(hopHeight);
        addSetting(timerSpeed);
        addSetting(friction);
        addSetting(airSpeed);
        addSetting(autoJump);
        addSetting(pauseInLiquid);
        addSetting(pauseSneak);
        addSetting(realPackets);
    }

    @Override public void onEnable() { groundTicks = 0; airTicks = 0; setTag(mode.get()); }
    @Override public void onDisable() { setTag(null); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.getAbilities().flying) return;
        if (pauseSneak.get() && mc.player.isSneaking()) return;
        if (pauseInLiquid.get() && (mc.player.isTouchingWater() || mc.player.isInLava())) return;

        setTag(mode.get());
        boolean onGround = mc.player.isOnGround();
        if (onGround) { groundTicks++; airTicks = 0; }
        else { airTicks++; groundTicks = 0; }

        switch (mode.get()) {
            case "Vanilla" -> tickVanilla();
            case "BHop" -> tickBHop(onGround);
            case "LowHop" -> tickLowHop(onGround);
            case "YPort" -> tickYPort(onGround);
            case "Strafe" -> tickStrafe(onGround);
            case "OnGround" -> tickOnGround(onGround);
            case "Timer" -> tickTimer();
            case "Custom" -> tickCustom(onGround);
            default -> tickBHop(onGround);
        }
    }

    private void tickVanilla() {
        if (!mc.player.isOnGround() || !isMoving()) return;
        Vec3d v = mc.player.getVelocity();
        double spd = horizontal(v);
        if (spd < 0.08 || spd > 0.4) return;
        double m = Math.min(speed.get(), 1.2);
        mc.player.setVelocity(v.x * m, v.y, v.z * m);
        maybeSync();
    }

    private void tickBHop(boolean onGround) {
        if (!isMoving()) return;
        if (onGround) {
            if (autoJump.get()) { mc.player.jump(); strafe(speed.get() * 0.9); }
            else strafe(speed.get() * 0.85);
        } else airAccelerate(airSpeed.get() * speed.get());
        maybeSync();
    }

    private void tickLowHop(boolean onGround) {
        if (!isMoving()) return;
        if (onGround) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, Math.min(hopHeight.get(), 0.32), v.z);
            strafe(speed.get() * 1.05);
        } else if (airTicks <= 2) {
            Vec3d v = mc.player.getVelocity();
            if (v.y > 0.1) mc.player.setVelocity(v.x, v.y * 0.4, v.z);
            airAccelerate(airSpeed.get());
        }
        maybeSync();
    }

    private void tickYPort(boolean onGround) {
        if (!isMoving()) return;
        if (onGround) {
            Vec3d v = mc.player.getVelocity();
            mc.player.setVelocity(v.x, hopHeight.get() * 0.85, v.z);
            strafe(speed.get());
        } else {
            Vec3d v = mc.player.getVelocity();
            if (airTicks > 1) mc.player.setVelocity(v.x, -0.2, v.z);
            strafe(speed.get() * 0.95);
        }
        maybeSync();
    }

    private void tickStrafe(boolean onGround) {
        if (!isMoving()) return;
        if (onGround && autoJump.get()) mc.player.jump();
        strafe(onGround ? speed.get() * 0.7 : speed.get() * 0.55);
        maybeSync();
    }

    private void tickOnGround(boolean onGround) {
        if (!onGround || !isMoving()) return;
        Vec3d v = mc.player.getVelocity();
        if (horizontal(v) < 0.05) return;
        double m = speed.get() * friction.get();
        mc.player.setVelocity(v.x * m, v.y, v.z * m);
        maybeSync();
    }

    private void tickTimer() {
        if (isMoving() && mc.player.isOnGround()) {
            Vec3d v = mc.player.getVelocity();
            double m = 1.0 + (timerSpeed.get() - 1.0) * 0.35;
            mc.player.setVelocity(v.x * m, v.y, v.z * m);
        }
    }

    private void tickCustom(boolean onGround) {
        if (!isMoving()) return;
        if (onGround) {
            if (autoJump.get()) {
                Vec3d v = mc.player.getVelocity();
                mc.player.setVelocity(v.x, hopHeight.get(), v.z);
            }
            strafe(speed.get());
        } else {
            airAccelerate(airSpeed.get() * speed.get());
            Vec3d v = mc.player.getVelocity();
            double f = 0.9 + friction.get() * 0.1;
            mc.player.setVelocity(v.x * f, v.y, v.z * f);
        }
        maybeSync();
    }

    private boolean isMoving() {
        try {
            return mc.player.input != null
                    && (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0);
        } catch (Throwable t) {
            return mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
                    || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
        }
    }

    private double horizontal(Vec3d v) { return Math.sqrt(v.x * v.x + v.z * v.z); }

    private void strafe(double targetSpeed) {
        float yaw = mc.player.getYaw();
        float forward, side;
        try {
            forward = mc.player.input.movementForward;
            side = mc.player.input.movementSideways;
        } catch (Throwable t) { return; }
        if (forward == 0 && side == 0) return;
        float angle = yaw;
        if (forward < 0) angle += 180;
        float sf = 90f * (forward == 0 ? 1f : (forward > 0 ? 0.5f : -0.5f));
        if (side > 0) angle -= sf;
        if (side < 0) angle += sf;
        double rad = Math.toRadians(angle);
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(-Math.sin(rad) * targetSpeed * 0.28, v.y, Math.cos(rad) * targetSpeed * 0.28);
    }

    private void airAccelerate(double accel) {
        float yaw = mc.player.getYaw();
        float forward, side;
        try {
            forward = mc.player.input.movementForward;
            side = mc.player.input.movementSideways;
        } catch (Throwable t) { return; }
        if (forward == 0 && side == 0) return;
        double rad = Math.toRadians(yaw);
        double wishX = -Math.sin(rad) * forward + Math.cos(rad) * side;
        double wishZ = Math.cos(rad) * forward + Math.sin(rad) * side;
        double len = Math.sqrt(wishX * wishX + wishZ * wishZ);
        if (len > 1e-6) { wishX = wishX / len * accel; wishZ = wishZ / len * accel; }
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x + wishX, v.y, v.z + wishZ);
    }

    private void maybeSync() {
        if (realPackets.get()) RealPackets.syncPosition();
    }

    private void setTag(String t) {
        try {
            var f = Module.class.getDeclaredField("tag");
            f.setAccessible(true);
            f.set(this, t);
        } catch (Throwable ignored) {}
    }
}
