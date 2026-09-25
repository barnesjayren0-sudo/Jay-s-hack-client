package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.vehicle.BoatEntity;

public class BoatFly extends Module {
    public final NumberSetting speed = new NumberSetting("Speed", "Horizontal speed", 1.5, 0.5, 5.0, 0.1);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Up/down speed", 0.8, 0.2, 3.0, 0.1);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Sync position", false);
    public BoatFly() { super("BoatFly", "Fly with boat", Category.MOVEMENT); addSetting(speed); addSetting(vertical); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null) return;
        if (!(mc.player.getVehicle() instanceof BoatEntity boat)) return;
        double sp = speed.get() * 0.5, vy = 0;
        if (mc.options.jumpKey.isPressed()) vy = vertical.get() * 0.4;
        if (mc.options.sneakKey.isPressed()) vy = -vertical.get() * 0.4;
        float yaw = mc.player.getYaw(); double mx = 0, mz = 0;
        if (mc.options.forwardKey.isPressed()) { mx -= Math.sin(Math.toRadians(yaw)) * sp; mz += Math.cos(Math.toRadians(yaw)) * sp; }
        if (mc.options.backKey.isPressed()) { mx += Math.sin(Math.toRadians(yaw)) * sp; mz -= Math.cos(Math.toRadians(yaw)) * sp; }
        if (mc.options.leftKey.isPressed()) { mx += Math.cos(Math.toRadians(yaw)) * sp; mz += Math.sin(Math.toRadians(yaw)) * sp; }
        if (mc.options.rightKey.isPressed()) { mx -= Math.cos(Math.toRadians(yaw)) * sp; mz -= Math.sin(Math.toRadians(yaw)) * sp; }
        boat.setVelocity(mx, vy, mz);
        if (realPackets.get()) RealPackets.syncPosition();
    }
}
