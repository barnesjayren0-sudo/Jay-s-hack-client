package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

/** Removes client block-break cooldown between breaks. */
public class NoBreakDelay extends Module {

    public NoBreakDelay() {
        super("NoBreakDelay", "No delay between block breaks", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc == null || mc.interactionManager == null) return;
        try {
            var f = mc.interactionManager.getClass().getDeclaredField("blockBreakingCooldown");
            f.setAccessible(true);
            f.setInt(mc.interactionManager, 0);
        } catch (Throwable t) {
            // mapping may differ — silent fail
        }
    }
}
