package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", "Soft KB — never full cancel", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (Humanizer.shouldSkipTick(15)) return;

        if (mc.player.hurtTime > 0 && mc.player.hurtTime < 9) {
            double f = ClientSettings.velocityFactor + (Humanizer.chance(40) ? 0.04 : 0);
            f = Math.min(0.95, f);
            mc.player.setVelocity(
                    mc.player.getVelocity().x * f,
                    mc.player.getVelocity().y,
                    mc.player.getVelocity().z * f
            );
        }
    }
}
