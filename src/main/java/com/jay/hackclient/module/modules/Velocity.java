package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", "Soft knockback reduction (not zero)", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Only shave a bit on hurt — zero KB is an easy flag
        if (mc.player.hurtTime != 8 && mc.player.hurtTime != 9) return;

        double h = MathUtil.randomDouble(0.55, 0.75);
        double v = MathUtil.randomDouble(0.9, 1.0);
        Vec3d vel = mc.player.getVelocity();
        mc.player.setVelocity(vel.x * h, vel.y * v, vel.z * h);
    }
}
