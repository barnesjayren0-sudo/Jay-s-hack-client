package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

/**
 * Reduce sprint reset after hitting (client-side assist).
 * Inspired by common PvP client "KeepSprint" modules — original implementation.
 */
public class KeepSprint extends Module {

    public KeepSprint() {
        super("KeepSprint", "Keep sprint momentum after attacks", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Soft: if moving forward and not blinded, re-assert sprint
        if (mc.player.forwardSpeed > 0.0f
                && !mc.player.isUsingItem()
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
