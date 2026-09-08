package com.jay.client18.module.modules;

import com.jay.client18.module.Module;

public class WTap extends Module {

    private long until;
    private boolean holding;
    private boolean wasSwing;

    public WTap() {
        super("WTap", "Sprint reset on hit", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        holding = false;
        until = 0;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        long now = System.currentTimeMillis();
        if (now < until) {
            mc.gameSettings.keyBindForward.pressed = false;
            holding = true;
            return;
        }
        if (holding) {
            holding = false;
        }

        boolean swinging = mc.thePlayer.isSwingInProgress;
        if (swinging && !wasSwing && mc.gameSettings.keyBindForward.isKeyDown()) {
            until = now + 70 + (long) (Math.random() * 30);
            mc.gameSettings.keyBindForward.pressed = false;
            holding = true;
        }
        wasSwing = swinging;
    }
}
