package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import org.lwjgl.glfw.GLFW;

/**
 * Ghost velocity — keep most horizontal KB, never 0%, Y untouched.
 * Occasional near-vanilla ticks break patterns.
 */
public class Velocity extends Module {

    public static long lastPacketMs = 0;
    private String lastMode = "";

    public final ModeSetting mode = new ModeSetting("Mode", "Preset", "Soft", "Soft", "Medium", "Strong", "Custom");
    public final NumberSetting horizontal = new NumberSetting("Horizontal", "Keep fraction", 0.68, 0.45, 1.0, 0.01);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Y keep (1=vanilla)", 1.0, 0.85, 1.0, 0.05);

    public Velocity() {
        super("Velocity", "Soft horizontal KB (ghost)", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
        addSetting(mode);
        addSetting(horizontal);
        addSetting(vertical);
    }

    @Override
    public void onTick() {
        String m = mode.get();
        if (!m.equals(lastMode) || "Custom".equals(m)) {
            lastMode = m;
            if (!"Custom".equals(m)) {
                ClientSettings.applyVelocityMode(m.toLowerCase());
                horizontal.set(ClientSettings.velocityHorizontal);
            } else {
                ClientSettings.velocityHorizontal = Math.max(0.45, horizontal.get());
                ClientSettings.velocityVertical = Math.max(0.85, vertical.get());
            }
        }
    }

    /** Factor used by mixin / packet path — includes humanizer. */
    public static double horizontalFactor() {
        double base = ClientSettings.velocityHorizontal;
        if (base < 0.45) base = 0.45;
        // Random near-vanilla hit
        if (Humanizer.chance(ClientSettings.velocityVanillaChance)) {
            return Math.min(0.95, base + 0.18);
        }
        // Tiny noise so it's never a flat multiplier
        double noise = (Math.random() * 0.06) - 0.03;
        return Math.max(0.45, Math.min(0.95, base + noise));
    }

    public static double verticalFactor() {
        return 1.0; // always leave Y alone for ghost
    }
}
