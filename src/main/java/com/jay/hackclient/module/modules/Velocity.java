package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.util.math.Vec3d;

public class Velocity extends Module {

    private final double horizontal = 0.4;
    private final double vertical = 1.0;

    public Velocity() {
        super("Velocity", "Reduces knockback after hits", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.hurtTime != 9) return; // peak knockback tick-ish

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * horizontal, v.y * vertical, v.z * horizontal);
    }
}
