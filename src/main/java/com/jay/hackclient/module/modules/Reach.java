package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;

/**
 * Extends entity attack reach via PlayerEntityMixin.
 * Vanilla = 3.0. Cap 3.45 — higher flags hard.
 */
public class Reach extends Module {

    public Reach() {
        super("Reach", "Attack range 3.0–3.45", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        ClientSettings.reachDistance = clamp(ClientSettings.reachDistance);
    }

    public static boolean isActive() {
        if (JayHackClient.moduleManager == null) return false;
        Module m = JayHackClient.moduleManager.getModuleByName("Reach");
        return m != null && m.isEnabled();
    }

    public static double getReach() {
        if (!isActive()) return 3.0;
        return clamp(ClientSettings.reachDistance);
    }

    public static double clamp(double v) {
        return Math.max(3.0, Math.min(3.45, v));
    }

    public static void setReach(double v) {
        ClientSettings.reachDistance = clamp(v);
    }
}
