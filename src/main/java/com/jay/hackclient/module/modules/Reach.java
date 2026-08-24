package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Range (Reach) — extends entity interaction range via mixin.
 * Keep values low (3.05–3.25) for less obvious play.
 */
public class Reach extends Module {

    public Reach() {
        super("Reach", "Attack range — keep near 3.1–3.2", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_UNKNOWN);
    }

    @Override
    public void onEnable() {
        // clamp to safe band on enable
        ClientSettings.reachDistance = clamp(ClientSettings.reachDistance);
    }

    public static boolean isActive() {
        if (JayHackClient.moduleManager == null) return false;
        Module m = JayHackClient.moduleManager.getModuleByName("Reach");
        return m != null && m.isEnabled();
    }

    /** Effective reach when module is on; otherwise vanilla. */
    public static double getReach() {
        if (!isActive()) return 3.0;
        return clamp(ClientSettings.reachDistance);
    }

    public static double clamp(double v) {
        // Hard cap 3.5 — higher is very obvious on anti-cheats
        return Math.max(3.0, Math.min(3.5, v));
    }

    public static void setReach(double v) {
        ClientSettings.reachDistance = clamp(v);
    }

    /** Presets for profiles / settings cycle. */
    public static void applyMode(String mode) {
        if (mode == null) mode = "soft";
        switch (mode.toLowerCase()) {
            case "strong" -> setReach(3.35);
            case "medium" -> setReach(3.2);
            default -> setReach(3.1); // soft
        }
        ClientSettings.reachMode = mode.toLowerCase();
    }
}
