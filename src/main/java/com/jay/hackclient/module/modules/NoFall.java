package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.util.RealPackets;

public class NoFall extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Packet", "Packet", "NoGround", "Spoof");
    public NoFall() { super("NoFall", "Prevent fall damage", Category.MOVEMENT); addSetting(mode); }
    @Override public void onTick() {
        if (mc.player == null || mc.player.fallDistance < 2.5f) return;
        if ("NoGround".equals(mode.get())) RealPackets.sendOnGround(false);
        else { RealPackets.sendOnGround(true); mc.player.fallDistance = 0; }
        setTag(mode.get());
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
