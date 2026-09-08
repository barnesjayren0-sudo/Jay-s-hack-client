package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.Humanizer;

/** Short sprint reset — never leaves W stuck. */
public class WTap extends Module {

    private long until;
    private boolean holding;
    private boolean wasSwing;

    public WTap() {
        super("WTap", "Quiet sprint reset", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        holding = false;
        until = 0;
        // do not force forward back on
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
            if (Humanizer.chance(20)) {
                wasSwing = swinging;
                return; // skip some taps
            }
            until = now + 55 + Humanizer.delay(15, 8);
            mc.gameSettings.keyBindForward.pressed = false;
            holding = true;
        }
        wasSwing = swinging;
    }
}
