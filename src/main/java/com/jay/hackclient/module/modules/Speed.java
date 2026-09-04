package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;

/** Soft ground speed — keep subtle for ghost play. */
public class Speed extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Legit", "Legit", "Strafe");
    public final NumberSetting multiplier = new NumberSetting("Speed", "Ground multiplier", 1.12, 1.0, 1.35, 0.01);

    public Speed() {
        super("Speed", "Soft movement speed", Category.MOVEMENT);
        addSetting(mode);
        addSetting(multiplier);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking() || mc.player.isUsingItem()) return;
        if (mc.player.horizontalCollision) return;

        double m = multiplier.get();
        if ("Legit".equals(mode.get())) m = Math.min(m, 1.15);

        var v = mc.player.getVelocity();
        double speed = Math.sqrt(v.x * v.x + v.z * v.z);
        if (speed < 0.08) return;
        if (speed > 0.35) return; // already fast

        double scale = m;
        mc.player.setVelocity(v.x * scale, v.y, v.z * scale);
    }
}
