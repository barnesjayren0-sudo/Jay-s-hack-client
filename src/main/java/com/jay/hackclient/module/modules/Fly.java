package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

/** Fly — Velocity / Creative / Packet modes. Public setTag, throttled packet sync. */
public class Fly extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Fly style", "Velocity", "Velocity", "Creative", "Packet");
    public final NumberSetting speed = new NumberSetting("Speed", "Fly speed", 0.8, 0.2, 5.0, 0.1);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Up/down speed", 0.5, 0.1, 3.0, 0.1);
    private long lastGroundPacket;

    public Fly() {
        super("Fly", "Velocity / creative / packet flight (easily server-flagged)", Category.MOVEMENT);
        addSetting(mode); addSetting(speed); addSetting(vertical);
    }

    @Override public void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        lastGroundPacket = 0;
        // Let the server see the take-off position before we start floating
        try { RealPackets.syncPosition(); } catch (Throwable ignored) {}
        if ("Creative".equals(mode.get())) mc.player.getAbilities().flying = true;
    }

    @Override public void onDisable() {
        if (mc.player != null) {
            try {
                mc.player.getAbilities().flying = false;
                mc.player.getAbilities().setFlySpeed(0.05f);
            } catch (Throwable ignored) {}
        }
        setTag(null);
    }

    @Override    public void onTick() {
        try { if (!com.jay.hackclient.util.CombatManager.canCombatModulesRun()) return; } catch (Throwable ignored) {}
        if (mc.player == null) return;
        setTag(mode.get());
        switch (mode.get()) {
            case "Creative" -> {
                mc.player.getAbilities().flying = true;
                mc.player.getAbilities().setFlySpeed((float) (speed.get() * 0.05));
            }
            case "Packet" -> {
                tickVelocity();
                long now = System.currentTimeMillis();
                if (now - lastGroundPacket >= 55) { // throttle: never every tick
                    lastGroundPacket = now;
                    RealPackets.sendOnGround(false);
                }
            }
            default -> tickVelocity();
        }
    }

    private void tickVelocity() {
        if (mc.player == null) return;
        double sp = speed.get() * 0.4, vy = 0;
        if (mc.options.jumpKey.isPressed()) vy = vertical.get() * 0.4;
        if (mc.options.sneakKey.isPressed()) vy = -vertical.get() * 0.4;
        float yaw = mc.player.getYaw();
        double mx = 0, mz = 0;
        boolean moving = false;
        if (mc.options.forwardKey.isPressed()) { mx -= Math.sin(Math.toRadians(yaw)); mz += Math.cos(Math.toRadians(yaw)); moving = true; }
        if (mc.options.backKey.isPressed()) { mx += Math.sin(Math.toRadians(yaw)); mz -= Math.cos(Math.toRadians(yaw)); moving = true; }
        if (mc.options.leftKey.isPressed()) { mx += Math.cos(Math.toRadians(yaw)); mz += Math.sin(Math.toRadians(yaw)); moving = true; }
        if (mc.options.rightKey.isPressed()) { mx -= Math.cos(Math.toRadians(yaw)); mz -= Math.sin(Math.toRadians(yaw)); moving = true; }
        if (moving) { double len = Math.sqrt(mx * mx + mz * mz); mx = mx / len * sp; mz = mz / len * sp; }
        mc.player.setVelocity(mx, vy, mz);
    }
}
