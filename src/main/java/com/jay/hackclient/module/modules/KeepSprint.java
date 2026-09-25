package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.RealPackets;

public class KeepSprint extends Module {
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Resync sprint packets", true);
    public KeepSprint() { super("KeepSprint", "Keep sprint after attacking", Category.MOVEMENT); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null) return;
        boolean moving = mc.options.forwardKey.isPressed()||mc.options.backKey.isPressed()||mc.options.leftKey.isPressed()||mc.options.rightKey.isPressed();
        if (!mc.player.isSprinting() && moving && !mc.player.isSneaking() && !mc.player.isUsingItem()) {
            mc.player.setSprinting(true);
            if (realPackets.get()) RealPackets.startSprint();
        }
        setTag(mc.player.isSprinting()?"ON":"OFF");
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
