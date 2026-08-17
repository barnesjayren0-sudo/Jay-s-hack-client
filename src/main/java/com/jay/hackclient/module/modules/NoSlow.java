package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", "Less slowdown while using items", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;

        // Soft client-side assist: keep some forward momentum while using
        if (mc.player.forwardSpeed > 0) {
            var v = mc.player.getVelocity();
            double scale = 1.15;
            mc.player.setVelocity(v.x * scale, v.y, v.z * scale);
        }
    }
}
