package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class WTap extends Module {

    private long lastTap = 0;
    private boolean released = false;

    public WTap() {
        super("WTap", "Sprint-reset style movement assist", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;

        long now = System.currentTimeMillis();
        // Simple periodic sprint reset while moving forward in combat range logic
        if (mc.player.forwardSpeed > 0 && mc.player.isSprinting()) {
            if (!released && now - lastTap > 600) {
                mc.player.setSprinting(false);
                released = true;
                lastTap = now;
            }
        }
        if (released && now - lastTap > 80) {
            if (mc.player.forwardSpeed > 0) {
                mc.player.setSprinting(true);
            }
            released = false;
        }
    }
}
