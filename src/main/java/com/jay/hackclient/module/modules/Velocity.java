package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Velocity 1.12.1 — packet-scaled KB + hurt-time cleanup.
 * Modes: soft / medium / strong via .jay velmode
 * Key: N
 */
public class Velocity extends Module {

    /** Set by mixin when a velocity packet for the player is reduced. */
    public static boolean packetHandledThisTick = false;

    public Velocity() {
        super("Velocity", "KB reduce soft/medium/strong — key N", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // Fallback / reinforce during hurtTime if packet path missed
        int ht = mc.player.hurtTime;
        if (ht <= 0 || ht > 10) {
            packetHandledThisTick = false;
            return;
        }

        // Don't strip jump motion completely
        boolean jumping = mc.player.input != null && mc.player.input.playerInput.jump();

        double hx = ClientSettings.velocityHorizontal;
        double hy = jumping ? 1.0 : ClientSettings.velocityVertical;

        // First 1–3 ticks after hit matter most — apply harder once
        if (ht >= 8) {
            Vec3d v = mc.player.getVelocity();
            // Only scale if still being knocked (horizontal speed notable)
            double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
            if (horiz > 0.08) {
                // slight random so not identical every hit
                double j = Humanizer.chance(50) ? 0.02 : -0.01;
                double fx = Math.max(0.25, Math.min(0.95, hx + j));
                double fy = Math.max(0.70, Math.min(1.0, hy));
                mc.player.setVelocity(v.x * fx, v.y * fy, v.z * fx);
            }
        } else if (ht >= 4 && !packetHandledThisTick) {
            Vec3d v = mc.player.getVelocity();
            double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
            if (horiz > 0.12) {
                mc.player.setVelocity(v.x * hx, v.y * hy, v.z * hx);
            }
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
