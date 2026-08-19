package com.jay.hackclient.module.modules;

import com.jay.hackclient.mixin.LivingEntityAccessor;
import com.jay.hackclient.module.Module;

/** Clears jump cooldown via accessor mixin. */
public class NoJumpDelay extends Module {

    public NoJumpDelay() {
        super("NoJumpDelay", "No client jump cooldown", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        try {
            ((LivingEntityAccessor) (Object) mc.player).jay$setJumpingCooldown(0);
        } catch (Throwable ignored) {
        }
    }
}
