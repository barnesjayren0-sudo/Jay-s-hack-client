package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import org.lwjgl.glfw.GLFW;

/**
 * Ghost velocity — chance to skip (vanilla), else soft horizontal keep.
 * Y always untouched.
 */
public class Velocity extends Module {

    public static long lastPacketMs = 0;
    private String lastMode = "";

    public final ModeSetting mode = new ModeSetting("Mode", "Preset", "Soft", "Soft", "Medium", "Strong", "Custom");
    public final NumberSetting horizontal = new NumberSetting("Horizontal", "Keep fraction", 0.68, 0.45, 1.0, 0.01);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Y keep (1=vanilla)", 1.0, 0.85, 1.0, 0.05);
    /** % of knockback packets we actually modify (rest stay vanilla). */
    public final NumberSetting chance = new NumberSetting("Chance", "% packets to modify", 85, 40, 100, 5);

    public Velocity() {
        super("Velocity", "Soft horizontal KB (ghost)", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
        addSetting(mode);
        addSetting(horizontal);
        addSetting(vertical);
        addSetting(chance);
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
        setTag(mode.get() + " " + (int) (horizontalFactor() * 100) + "%");
    }

    /**
     * Factor for mixin. Returns 1.0 when skipping (full vanilla).
     */
    public static double horizontalFactor() {
        double base = ClientSettings.velocityHorizontal;
        if (base < 0.45) base = 0.45;

        int ch = 85;
        try {
            Module mod = com.jay.hackclient.JayHackClient.moduleManager != null
                    ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Velocity") : null;
            if (mod instanceof Velocity v) {
                ch = v.chance.getInt();
                if ("Custom".equals(v.mode.get())) base = Math.max(0.45, v.horizontal.get());
            }
        } catch (Throwable ignored) {}

        // Skip = leave packet vanilla (LB Modify-style chance)
        if (!Humanizer.chance(ch)) {
            return 1.0;
        }

        if (Humanizer.chance(ClientSettings.velocityVanillaChance)) {
            return Math.min(0.95, base + 0.18);
        }

        double noise = (Math.random() * 0.06) - 0.03;
        return Math.max(0.45, Math.min(0.95, base + noise));
    }

    public static double verticalFactor() {
        return 1.0;
    }
}
