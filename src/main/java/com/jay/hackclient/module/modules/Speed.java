package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {

    private final double multiplier = 1.12;

    public Speed() {
        super("Speed", "Slightly faster ground movement", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.forwardSpeed == 0 && mc.player.sidewaysSpeed == 0) return;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * multiplier, v.y, v.z * multiplier);
    }
}
