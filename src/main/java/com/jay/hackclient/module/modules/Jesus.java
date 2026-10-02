package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.util.math.Vec3d;

/** Jesus — walk on water / lava. Public setTag, throttled ground packet. */
public class Jesus extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Solid", "Solid", "Boost", "Dolphin");
    public final NumberSetting speed = new NumberSetting("Speed", "Water walk speed", 1.1, 0.8, 2.0, 0.05);private long lastGroundPacket;
    private long lastBoost;

    public Jesus() { super("Jesus", "Walk on water / lava", Category.MOVEMENT); addSetting(mode); addSetting(speed); }

    @Override public void onDisable() { setTag(null); }

    @Override public void onTick() {
        if (mc.player == null || mc.world == null) { setTag(null); return; }
        if (!(mc.player.isTouchingWater() || mc.player.isInLava())) { setTag(null); return; }
        setTag(mode.get());
        Vec3d v = mc.player.getVelocity();
        switch (mode.get()) {
            case "Solid" -> {
                if (v.y < 0) mc.player.setVelocity(v.x, 0, v.z);
                mc.player.setOnGround(true);
                long now = System.currentTimeMillis();
                if (now - lastGroundPacket >= 100) {
                    lastGroundPacket = now;
                    RealPackets.sendOnGround(true);
                }
            }
            case "Boost" -> {
                // Throttle + cap, otherwise every tick stacks another push on top
                // of the previous one and the player skates off the world.
                long now = System.currentTimeMillis();
                if (now - lastBoost >= 100) {
                    lastBoost = now;
                    double cap = speed.get() * 0.25;
                    float yaw = mc.player.getYaw();
                    double nx = MathUtil.clamp(-Math.sin(Math.toRadians(yaw)) * cap, -cap, cap);
                    double nz = MathUtil.clamp(Math.cos(Math.toRadians(yaw)) * cap, -cap, cap);
                    mc.player.setVelocity(nx, Math.max(v.y, 0.02), nz);
                }
            }
            case "Dolphin" -> {
                if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(v.x, 0.4, v.z);
                else if (v.y < 0) mc.player.setVelocity(v.x, v.y * 0.5, v.z);
            }
            default -> {}
        }
    }
}
