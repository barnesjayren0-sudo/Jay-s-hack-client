package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Horizontal KB reduction via packet mixin. Y never touched.
 */
public class Velocity extends Module {

    public static long lastPacketMs = 0;

    public Velocity() {
        super("Velocity", "Horizontal KB reduce — bind [N]", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {}

    public static void applyVelocityMode(String mode) {
        ClientSettings.applyVelocityMode(mode);
    }

    public static double horizontalFactor() {
        // Never below 0.40 — zero velocity is free ban
        return Math.max(0.40, Math.min(0.95, ClientSettings.velocityHorizontal));
    }

    public static double verticalFactor() {
        return 1.0;
    }
}
