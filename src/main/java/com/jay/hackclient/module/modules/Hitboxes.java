package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import org.lwjgl.glfw.GLFW;

/**
 * Expands OTHER players' hitboxes client-side so attacks / aim land easier.
 * Keep expand low (0.08–0.15) for less obvious play. Hard-capped at 0.40.
 */
public class Hitboxes extends Module {

    public static double expand = 0.12;

    public Hitboxes() {
        super("Hitboxes", "Bigger enemy hitboxes — keep low", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_H);
    }

    @Override
    public void onEnable() {
        expand = clamp(ClientSettings.hitboxExpand);
        ClientSettings.hitboxExpand = expand;
    }

    public static boolean isActive() {
        if (JayHackClient.moduleManager == null) return false;
        Module m = JayHackClient.moduleManager.getModuleByName("Hitboxes");
        return m != null && m.isEnabled();
    }

    public static double getExpand() {
        if (!isActive()) return 0.0;
        return clamp(ClientSettings.hitboxExpand);
    }

    public static void setExpand(double value) {
        expand = clamp(value);
        ClientSettings.hitboxExpand = expand;
    }

    public static double clamp(double v) {
        return Math.max(0.0, Math.min(0.40, v));
    }
}
