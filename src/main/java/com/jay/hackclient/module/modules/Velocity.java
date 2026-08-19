package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Velocity — packet scale + hurtTime reinforce.
 * .jay velmode soft|medium|strong
 * Key: N
 */
public class Velocity extends Module {

    public static boolean packetHandledThisTick = false;

    public Velocity() {
        super("Velocity", "KB reduce soft/medium/strong — key N", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        int ht = mc.player.hurtTime;
        if (ht <= 0 || ht > 10) {
            packetHandledThisTick = false;
            return;
        }

        boolean jumping = mc.options != null && mc.options.jumpKey.isPressed();

        double hx = horizontalFactor();
        double hy = jumping ? 1.0 : verticalFactor();

        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);

        // Strongest right after the hit packet
        if (ht >= 8 && horiz > 0.05) {
            double j = Humanizer.chance(50) ? 0.02 : -0.015;
            double fx = Math.max(0.25, Math.min(0.95, hx + j));
            mc.player.setVelocity(v.x * fx, v.y * hy, v.z * fx);
        } else if (ht >= 3 && horiz > 0.10 && !packetHandledThisTick) {
            mc.player.setVelocity(v.x * hx, v.y * hy, v.z * hx);
        }

        packetHandledThisTick = false;
    }

    public static double horizontalFactor() {
        return Math.max(0.25, Math.min(0.95, ClientSettings.velocityHorizontal));
    }

    public static double verticalFactor() {
        return Math.max(0.70, Math.min(1.0, ClientSettings.velocityVertical));
    }
}
