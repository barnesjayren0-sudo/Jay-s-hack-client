package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Horizontal knockback reduction via EntityVelocityUpdateS2CPacketMixin.
 * Y is never modified. Factor always follows ClientSettings (config/GUI).
 */
public class Velocity extends Module {

    public static long lastPacketMs = 0;

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
        // empty — packet mixin only
    }

    public static void applyVelocityMode(String mode) {
        ClientSettings.applyVelocityMode(mode);
    }

    /** Always from ClientSettings so config / .jay set / GUI stay in sync. */
    public static double horizontalFactor() {
        return Math.max(0.40, Math.min(0.95, ClientSettings.velocityHorizontal));
    }

    public static double verticalFactor() {
        return 1.0;
    }
}
