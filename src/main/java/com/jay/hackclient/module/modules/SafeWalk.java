package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.util.math.BlockPos;

/** Sneak at block edges while on ground (scaffold-friendly). */
public class SafeWalk extends Module {

    public SafeWalk() {
        super("SafeWalk", "Sneak at edges", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.options == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isSneaking()) return;

        // if air under feet slightly ahead of movement, force sneak key
        double yaw = Math.toRadians(mc.player.getYaw());
        double fx = -Math.sin(yaw) * 0.3;
        double fz = Math.cos(yaw) * 0.3;
        BlockPos ahead = BlockPos.ofFloored(
                mc.player.getX() + fx,
                mc.player.getY() - 0.1,
                mc.player.getZ() + fz
        );
        BlockPos under = mc.player.getBlockPos().down();

        boolean edge = mc.world.getBlockState(ahead).isAir()
                || mc.world.getBlockState(under).isAir();

        if (edge && (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0)) {
            mc.player.setSneaking(true);
            // also press key state so server sees sneak when possible
            try {
                mc.options.sneakKey.setPressed(true);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            try { mc.options.sneakKey.setPressed(false); } catch (Exception ignored) {}
        }
    }
}
