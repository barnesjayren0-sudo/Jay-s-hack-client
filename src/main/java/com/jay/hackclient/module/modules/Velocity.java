package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Packet-side horizontal KB only — NO per-tick setVelocity (that caused stutter).
 * Key: N
 */
public class Velocity extends Module {

    public static boolean packetHandledThisTick = false;
    public static long lastPacketMs = 0;

    public Velocity() {
        super("Velocity", "Horizontal KB on hit only — key N", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        // Intentionally empty — all work is in ClientPlayNetworkHandlerMixin
        // Applying setVelocity every tick while hurt caused the wiggle/stutter
        packetHandledThisTick = false;
    }

    public static double horizontalFactor() {
        return Math.max(0.45, Math.min(0.90, ClientSettings.velocityHorizontal));
    }

    public static double verticalFactor() {
        return 1.0;
    }
}
