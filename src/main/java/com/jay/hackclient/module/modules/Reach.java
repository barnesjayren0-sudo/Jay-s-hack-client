package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;

/** Soft reach — default barely above vanilla. */
public class Reach extends Module {

    private static double reach = 3.05;

    public final NumberSetting distance = new NumberSetting("Distance", "Attack range", 3.05, 3.0, 3.25, 0.01);

    public Reach() {
        super("Reach", "Tiny reach extension (ghost)", Category.COMBAT);
        addSetting(distance);
    }

    @Override
    public void onTick() {
        reach = Math.min(3.25, distance.get());
        ClientSettings.reachDistance = reach;
    }

    @Override
    public void onDisable() {
        reach = 3.0;
    }

    public static boolean isActive() {
        try {
            if (com.jay.hackclient.JayHackClient.moduleManager == null) return false;
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Reach");
            return m != null && m.isEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public static double getReach() {
        return isActive() ? reach : 3.0;
    }

    public static void setReach(double v) {
        reach = Math.max(3.0, Math.min(3.25, v));
    }
}
