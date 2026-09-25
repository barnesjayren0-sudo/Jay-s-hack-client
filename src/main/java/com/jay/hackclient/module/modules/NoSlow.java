package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

public class NoSlow extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Soft", "Soft", "Vanilla", "Grim");
    public final BoolSetting onlyShield = new BoolSetting("Shield Only", "Only while blocking", false);
    public final NumberSetting factor = new NumberSetting("Factor", "Movement scale", 0.9, 0.5, 1.0, 0.05);
    public NoSlow() {
        super("NoSlow", "Less slowdown while using items", Category.MOVEMENT);
        addSetting(mode); addSetting(onlyShield); addSetting(factor);
    }
    @Override public void onTick() {
        if (mc.player == null || !mc.player.isUsingItem()) return;
        if (onlyShield.get() && !mc.player.isBlocking()) return;
        if ("Soft".equals(mode.get())) {
            double f = factor.get();
            var v = mc.player.getVelocity();
            if (mc.player.isOnGround())
                mc.player.setVelocity(v.x / Math.max(0.2, f) * 0.85, v.y, v.z / Math.max(0.2, f) * 0.85);
        } else if ("Grim".equals(mode.get()) && mc.player.isSprinting()) {
            RealPackets.stopSprint(); RealPackets.startSprint();
        }
        setTag(mode.get());
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
