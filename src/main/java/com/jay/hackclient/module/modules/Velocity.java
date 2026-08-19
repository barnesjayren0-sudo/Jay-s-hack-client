package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Quiet velocity — only reduces KB while hurtTime > 0.
 * Never cancels packets (that stopped movement).
 *
 * Recommended: .jay velmode soft  or  medium
 * Avoid strong on strict AC.
 * Key: N
 */
public class Velocity extends Module {

    public static boolean packetHandledThisTick = false;
    public static long lastPacketMs = 0;

    public Velocity() {
        super("Velocity", "Soft KB only while hurt — key N", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // Clear flag; do not spam setVelocity every tick (flags + feels sticky)
        if (packetHandledThisTick) {
            packetHandledThisTick = false;
            return;
        }

        int ht = mc.player.hurtTime;
        if (ht <= 0 || ht > 8) return;

        // Very light residual only if still sliding hard after a hit
        long now = System.currentTimeMillis();
        if (now - lastPacketMs < 80) return;

        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        if (horiz < 0.20) return;

        double hx = Math.max(0.40, horizontalFactor());
        boolean jumping = mc.options != null && mc.options.jumpKey.isPressed();
        double vy = jumping ? v.y : v.y * Math.max(0.85, verticalFactor());

        mc.player.setVelocity(v.x * hx, vy, v.z * hx);
    }

    public static double horizontalFactor() {
        // Floor at 0.35 so we never look like full anti-KB
        return Math.max(0.35, Math.min(0.95, ClientSettings.velocityHorizontal));
    }

    public static double verticalFactor() {
        return Math.max(0.80, Math.min(1.0, ClientSettings.velocityVertical));
    }
}
