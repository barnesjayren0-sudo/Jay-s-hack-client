package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.RealPackets;

public class AutoSprint extends Module {
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Sprint command packets", true);
    public final BoolSetting omni = new BoolSetting("Omni", "All directions", false);
    public AutoSprint() { super("AutoSprint", "Always sprint when moving", Category.MOVEMENT); addSetting(realPackets); addSetting(omni); }
    @Override public void onTick() {
        if (mc.player == null || mc.player.isSneaking() || mc.player.isUsingItem()) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        boolean move = omni.get()
            ? (mc.options.forwardKey.isPressed()||mc.options.backKey.isPressed()||mc.options.leftKey.isPressed()||mc.options.rightKey.isPressed())
            : mc.options.forwardKey.isPressed();
        if (move && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
            if (realPackets.get()) RealPackets.startSprint();
        }
        setTag(mc.player.isSprinting()?"ON":null);
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
