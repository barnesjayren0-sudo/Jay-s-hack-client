package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.util.math.BlockPos;

/**
 * Sneak only while standing at an edge — releases sneak when safe again (not sticky).
 */
public class SafeWalk extends Module {

    /** True when this module pressed sneak; we only release if we were the ones who pressed. */
    private boolean weForcedSneak;

    public SafeWalk() {
        super("SafeWalk", "Sneak at edges (non-sticky)", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        releaseSneakIfOurs();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.options == null) {
            releaseSneakIfOurs();
            return;
        }

        if (!mc.player.isOnGround()) {
            releaseSneakIfOurs();
            return;
        }

        boolean moving = Math.abs(mc.player.forwardSpeed) > 0.01f
                || Math.abs(mc.player.sidewaysSpeed) > 0.01f
                || mc.options.forwardKey.isPressed()
                || mc.options.backKey.isPressed()
                || mc.options.leftKey.isPressed()
                || mc.options.rightKey.isPressed();

        if (!moving) {
            releaseSneakIfOurs();
            return;
        }

        if (isNearEdge()) {
            // only force if player isn't already holding sneak manually
            if (!mc.options.sneakKey.isPressed() || weForcedSneak) {
                mc.options.sneakKey.setPressed(true);
                weForcedSneak = true;
            }
        } else {
            releaseSneakIfOurs();
        }
    }

    private boolean isNearEdge() {
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        // check center under feet + small offsets in move direction
        double yaw = Math.toRadians(mc.player.getYaw());
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double sx = Math.cos(yaw);
        double sz = Math.sin(yaw);

        // sample points slightly ahead / left / right of feet
        double[][] offsets = {
                {0, 0},
                {fx * 0.35, fz * 0.35},
                {fx * 0.55, fz * 0.55},
                {sx * 0.3, sz * 0.3},
                {-sx * 0.3, -sz * 0.3},
        };

        for (double[] o : offsets) {
            BlockPos under = BlockPos.ofFloored(x + o[0], y - 0.2, z + o[1]);
            if (mc.world.getBlockState(under).isAir()) {
                return true;
            }
        }
        return false;
    }

    private void releaseSneakIfOurs() {
        if (!weForcedSneak) return;
        weForcedSneak = false;
        if (mc.options == null) return;
        try {
            // only unpress if player is not holding the physical sneak key
            long handle = mc.getWindow() != null ? mc.getWindow().getHandle() : 0;
            boolean physical = false;
            if (handle != 0) {
                int key = mc.options.sneakKey.getDefaultKey().getCode();
                physical = org.lwjgl.glfw.GLFW.glfwGetKey(handle, key) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            }
            if (!physical) {
                mc.options.sneakKey.setPressed(false);
            }
        } catch (Exception e) {
            try {
                mc.options.sneakKey.setPressed(false);
            } catch (Exception ignored) {}
        }
    }
}
