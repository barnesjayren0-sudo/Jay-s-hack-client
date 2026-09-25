package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.util.math.Vec3d;

public class Jesus extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Solid", "Solid", "Boost", "Dolphin");
    public final NumberSetting speed = new NumberSetting("Speed", "Water walk speed", 1.1, 0.8, 2.0, 0.05);
    public Jesus() { super("Jesus", "Walk on water / lava", Category.MOVEMENT); addSetting(mode); addSetting(speed); }
    @Override public void onTick() {
        if (mc.player == null) return;
        if (!(mc.player.isTouchingWater() || mc.player.isInLava())) { setTag(null); return; }
        setTag(mode.get());
        Vec3d v = mc.player.getVelocity();
        switch (mode.get()) {
            case "Solid" -> { if (v.y < 0) mc.player.setVelocity(v.x, 0, v.z); mc.player.setOnGround(true); RealPackets.sendOnGround(true); }
            case "Boost" -> {
                double s = speed.get()*0.25; float yaw = mc.player.getYaw();
                mc.player.setVelocity(-Math.sin(Math.toRadians(yaw))*s, Math.max(v.y,0.02), Math.cos(Math.toRadians(yaw))*s);
            }
            case "Dolphin" -> {
                if (mc.options.jumpKey.isPressed()) mc.player.setVelocity(v.x, 0.4, v.z);
                else if (v.y < 0) mc.player.setVelocity(v.x, v.y*0.5, v.z);
            }
            default -> {}
        }
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
