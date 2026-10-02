package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.RealPackets;

/** AutoSprint — always sprint when moving. Public setTag. */
public class AutoSprint extends Module {
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Sprint command packets", true);
    public final BoolSetting omni = new BoolSetting("Omni", "All directions", false);

    /** True only while this module is the one holding the sprint. */
    private boolean forced;
    public AutoSprint() { super("AutoSprint", "Always sprint when moving", Category.MOVEMENT); addSetting(realPackets); addSetting(omni); }

    @Override public void onDisable() {
        // Stop the sprint we started so the player isn't left running
        try {
            if (mc.player != null && forced && mc.player.isSprinting()) {
                mc.player.setSprinting(false);
                if (realPackets.get()) RealPackets.stopSprint();
            }
        } catch (Throwable ignored) {}
        forced = false;
        setTag(null);
    }

    @Override public void onTick() {
        if (mc.player == null || mc.world == null) { setTag(null); return; }
        if (mc.player.isSneaking() || mc.player.isUsingItem()) { setTag(null); return; }
        if (mc.player.getHungerManager().getFoodLevel() <= 6) { setTag(null); return; }
        boolean move = omni.get()
                ? (mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed()
                   || mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed())
                : mc.options.forwardKey.isPressed();
        if (move && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
            forced = true;
            if (realPackets.get()) RealPackets.startSprint();
        }
        setTag(mc.player.isSprinting() ? "ON" : null);
    }
}
