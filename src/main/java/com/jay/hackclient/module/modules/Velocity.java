package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Horizontal knockback reduction.
 * Does not modify Y velocity.
 * Handled only on velocity packet (mixin) — no per-tick setVelocity.
 */
public class Velocity extends Module {

    public static long lastPacketMs = 0;

    // Default: keep 55% of horizontal knockback
    public static double velocityHorizontal = 0.55;

    // Y is never modified
    public static double velocityVertical = 1.0;

    public Velocity() {
        super(
            "Velocity",
            "Reduces horizontal knockback",
            Category.COMBAT
        );

        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        // Intentionally empty.
        // Velocity is handled when the knockback packet arrives.
    }

    /**
     * soft   = 0.55
     * medium = 0.50
     * strong = 0.42
     */
    public static void applyVelocityMode(String mode) {
        if (mode == null) {
            velocityHorizontal = 0.55;
            ClientSettings.velocityHorizontal = 0.55;
            ClientSettings.velocityMode = "soft";
            return;
        }

        switch (mode.toLowerCase()) {
            case "soft" -> {
                velocityHorizontal = 0.55;
                ClientSettings.velocityMode = "soft";
            }
            case "medium" -> {
                velocityHorizontal = 0.50;
                ClientSettings.velocityMode = "medium";
            }
            case "strong" -> {
                velocityHorizontal = 0.42;
                ClientSettings.velocityMode = "strong";
            }
            default -> {
                velocityHorizontal = 0.55;
                ClientSettings.velocityMode = "soft";
            }
        }
        ClientSettings.velocityHorizontal = velocityHorizontal;
        ClientSettings.velocityVertical = 1.0;
    }

    public static double horizontalFactor() {
        return velocityHorizontal;
    }

    public static double verticalFactor() {
        return velocityVertical; // always 1.0
    }
}
