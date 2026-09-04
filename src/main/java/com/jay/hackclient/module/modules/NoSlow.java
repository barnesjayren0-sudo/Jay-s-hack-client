package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;

/** Soft item-use slowdown reduce — not full noslow. */
public class NoSlow extends Module {

    public final BoolSetting onlyShield = new BoolSetting("ShieldOnly", "Only while blocking", false);
    public final NumberSetting factor = new NumberSetting("Factor", "Movement scale while using", 0.85, 0.6, 1.0, 0.05);

    public NoSlow() {
        super("NoSlow", "Less slowdown while using items", Category.MOVEMENT);
        addSetting(onlyShield);
        addSetting(factor);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isUsingItem()) return;
        if (onlyShield.get() && !mc.player.isBlocking()) return;

        double f = factor.get();
        var v = mc.player.getVelocity();
        // Soft: only boost if clearly slowed (not air control abuse)
        if (mc.player.isOnGround() && Math.abs(v.x) + Math.abs(v.z) < 0.12) {
            mc.player.setVelocity(v.x * (1.0 + (1.0 - f) * 0.5), v.y, v.z * (1.0 + (1.0 - f) * 0.5));
        }
    }
}
