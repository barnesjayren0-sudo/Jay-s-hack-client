package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Horizontal knockback reduction — Y never modified by default.
 * Actual packet scaling is applied in EntityVelocity mixin.
 */
public class Velocity extends Module {

    public static long lastPacketMs = 0;

    public final ModeSetting mode = new ModeSetting("Mode", "Preset strength", "Soft", "Soft", "Medium", "Strong", "Custom");
    public final NumberSetting horizontal = new NumberSetting("Horizontal", "Keep fraction of KB", 0.55, 0.35, 1.0, 0.01);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Keep Y (1=vanilla)", 1.0, 0.5, 1.0, 0.05);

    public Velocity() {
        super("Velocity", "Reduce horizontal knockback", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
        addSetting(mode);
        addSetting(horizontal);
        addSetting(vertical);
    }

    @Override
    public void onTick() {
        // Sync mode presets into ClientSettings used by mixin
        String m = mode.get();
        if (!"Custom".equals(m)) {
            ClientSettings.applyVelocityMode(m.toLowerCase());
            horizontal.set(ClientSettings.velocityHorizontal);
        } else {
            ClientSettings.velocityHorizontal = horizontal.get();
            ClientSettings.velocityVertical = vertical.get();
        }
    }

    public static double horizontalFactor() {
        return ClientSettings.velocityHorizontal;
    }

    public static double verticalFactor() {
        return ClientSettings.velocityVertical;
    }
}
