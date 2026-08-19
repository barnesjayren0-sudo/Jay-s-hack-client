package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", "Soft knockback reduce", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (Humanizer.shouldSkipTick(12)) return;

        if (mc.player.hurtTime > 0 && mc.player.hurtTime < 9) {
            double f = 0.68 + (Humanizer.chance(50) ? 0.06 : 0.0);
            mc.player.setVelocity(
                    mc.player.getVelocity().x * f,
                    mc.player.getVelocity().y,
                    mc.player.getVelocity().z * f
            );
        }
    }
}
