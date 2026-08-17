package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.util.math.Vec3d;

public class Speed extends Module {

    public Speed() {
        super("Speed", "Mild ground speed boost", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.forwardSpeed == 0 && mc.player.sidewaysSpeed == 0) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;

        double mult = MathUtil.randomDouble(1.06, 1.10);
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * mult, v.y, v.z * mult);
    }
}
