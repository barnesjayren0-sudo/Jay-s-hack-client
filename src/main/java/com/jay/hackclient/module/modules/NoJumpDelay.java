package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

/** Removes client jump cooldown for faster jump-resets in sword. */
public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "No client jump cooldown", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // jumpingCooldown field — try reset each tick while on ground readiness
        try {
            mc.player.jumpingCooldown = 0;
        } catch (Throwable ignored) {
            // field name may differ; ignore if inaccessible
        }
    }
}
