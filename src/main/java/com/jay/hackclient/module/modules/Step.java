package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

public class Step extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Vanilla", "Vanilla", "Packet");
    public final NumberSetting height = new NumberSetting("Height", "Step height", 1.0, 0.6, 2.5, 0.1);
    private float oldStep = 0.6f;
    public Step() { super("Step", "Step up blocks", Category.MOVEMENT); addSetting(mode); addSetting(height); }
    @Override public void onEnable() { if (mc.player != null) try { oldStep = mc.player.getStepHeight(); } catch (Throwable ignored) {} }
    @Override public void onDisable() { if (mc.player != null) try { mc.player.setStepHeight(oldStep); } catch (Throwable ignored) {} setTag(null); }
    @Override public void onTick() {
        if (mc.player == null) return;
        setTag(String.format("%.1f", height.get()));
        try { mc.player.setStepHeight((float)height.get()); } catch (Throwable ignored) {}
        if ("Packet".equals(mode.get()) && mc.player.horizontalCollision && mc.player.isOnGround()) {
            RealPackets.sendPosition(mc.player.getX(), mc.player.getY()+height.get()*0.5, mc.player.getZ(), false);
            RealPackets.sendPosition(mc.player.getX(), mc.player.getY()+height.get(), mc.player.getZ(), true);
        }
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
