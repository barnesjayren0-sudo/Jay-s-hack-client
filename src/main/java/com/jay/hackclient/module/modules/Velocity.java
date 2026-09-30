package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/** Velocity — knockback reduction. Chance + horizontal/vertical factors, real reduction logic via mixin. */
public class Velocity extends Module {

    public static long lastPacketMs = 0;
    private String lastMode = "";

    public final ModeSetting mode = new ModeSetting("Mode", "Preset", "Soft", "Soft", "Medium", "Strong", "Custom", "JumpReset");
    public final NumberSetting horizontal = new NumberSetting("Horizontal", "Keep fraction", 0.68, 0.0, 1.0, 0.01);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Y keep", 1.0, 0.0, 1.0, 0.05);
    public final NumberSetting chance = new NumberSetting("Chance", "% packets to modify", 90, 10, 100, 5);

    public Velocity() {
        super("Velocity", "Knockback reduction (ghost)", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
        addSetting(mode); addSetting(horizontal); addSetting(vertical); addSetting(chance);
    }

    @Override
    public void onTick() {
        String m = mode.get();
        if (!m.equals(lastMode) || "Custom".equals(m)) {
            lastMode = m;
            switch (m) {
                case "Soft" -> { horizontal.set(0.85); vertical.set(1.0); }
                case "Medium" -> { horizontal.set(0.55); vertical.set(1.0); }
                case "Strong" -> { horizontal.set(0.15); vertical.set(0.9); }
                case "JumpReset" -> { horizontal.set(0.70); vertical.set(1.0); }
                default -> {}
            }
            try {
                ClientSettings.velocityHorizontal = horizontal.get();
                ClientSettings.velocityVertical = vertical.get();
            } catch (Throwable ignored) {}
        }
        setTag(m + " " + (int) (horizontal.get() * 100) + "%");
    }

    /** Reduction factors actually applied by EntityVelocityUpdateS2CPacketMixin. */
    public static double horizontalFactor() {
        Velocity v = instance();
        if (v == null) return 1.0;
        if (Math.random() * 100 > v.chance.get()) return 1.0;
        return Math.max(0.0, v.horizontal.get());
    }

    public static double verticalFactor() {
        Velocity v = instance();
        if (v == null) return 1.0;
        if (Math.random() * 100 > v.chance.get()) return 1.0;
        return Math.max(0.0, v.vertical.get());
    }

    public static boolean isActive() {
        return instance() != null;
    }

    private static Velocity instance() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Velocity");
            return (m instanceof Velocity v && v.isEnabled()) ? v : null;
        } catch (Throwable t) { return null; }
    }
}
