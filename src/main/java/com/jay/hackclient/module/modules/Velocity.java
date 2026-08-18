package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;

/** Soft velocity — reduces knockback slightly, not full cancel. */
public class Velocity extends Module {

    public Velocity() {
        super("Velocity", "Soft KB reduction (not 0%)", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (Humanizer.shouldSkipTick(15)) return;

        // Only trim horizontal when recently hurt — partial, not zero
        if (mc.player.hurtTime > 0 && mc.player.hurtTime < 8) {
            double factor = 0.72 + (Humanizer.chance(50) ? 0.05 : 0); // ~72–77%
            mc.player.setVelocity(
                    mc.player.getVelocity().x * factor,
                    mc.player.getVelocity().y,
                    mc.player.getVelocity().z * factor
            );
        }
    }
}
