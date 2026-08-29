package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Light speed boost — soft modes for mobile/legit. */
public class Speed extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Boost style", "Strafe", "Strafe", "Ground", "Off");
    public final NumberSetting multiplier = new NumberSetting("Multiplier", "Speed factor", 1.12, 1.0, 1.35, 0.01);

    public Speed() {
        super("Speed", "Soft speed boost", Category.MOVEMENT);
        addSetting(mode);
        addSetting(multiplier);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if ("Off".equals(mode.get())) return;
        if (mc.player.getAbilities().flying || mc.player.isGliding()) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        double m = multiplier.get();
        if (m <= 1.0) return;

        if ("Ground".equals(mode.get()) && !mc.player.isOnGround()) return;

        Vec3d v = mc.player.getVelocity();
        double hx = v.x;
        double hz = v.z;
        double speed = Math.sqrt(hx * hx + hz * hz);
        if (speed < 0.05) return;

        // Cap so it doesn't look blatant
        double factor = Math.min(m, 1.28);
        mc.player.setVelocity(hx * factor, v.y, hz * factor);
    }
}
