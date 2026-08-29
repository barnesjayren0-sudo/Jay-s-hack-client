package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/** Walk on water / lava surface. */
public class Jesus extends Module {

    public Jesus() {
        super("Jesus", "Walk on water and lava", Category.ANARCHY);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isSneaking()) return; // hold shift to sink

        boolean inFluid = mc.player.isTouchingWater() || mc.player.isInLava();
        BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.2, mc.player.getZ());
        boolean fluidBelow = mc.world.getBlockState(below).isOf(Blocks.WATER)
                || mc.world.getBlockState(below).isOf(Blocks.LAVA)
                || mc.world.getBlockState(below).isOf(Blocks.BUBBLE_COLUMN);

        if (inFluid || fluidBelow) {
            Vec3d v = mc.player.getVelocity();
            // Keep on surface
            if (mc.player.getY() % 1 < 0.9 || inFluid) {
                mc.player.setVelocity(v.x, Math.max(v.y, 0.02), v.z);
            }
            mc.player.setOnGround(true);
            mc.player.fallDistance = 0;
        }
    }
}
