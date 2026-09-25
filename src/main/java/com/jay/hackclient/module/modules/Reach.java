package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;

/** Reach — soft attack range (no custom packets). */
public class Reach extends Module {

    private static double reach = 3.05;
    public final NumberSetting distance = new NumberSetting("Distance", "Attack range", 3.1, 3.0, 4.0, 0.05);

    public Reach() {
        super("Reach", "Attack range extension", Category.COMBAT);
        addSetting(distance);
    }

    @Override public void onTick() {
        reach = distance.get();
        try { ClientSettings.reachDistance = reach; } catch (Throwable ignored) {}
        setTag(String.format("%.2f", reach));
    }

    @Override public void onDisable() {
        reach = 3.0;
        try { ClientSettings.reachDistance = 3.0; } catch (Throwable ignored) {}
        setTag(null);
    }

    public static boolean isActive() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Reach");
            return m != null && m.isEnabled();
        } catch (Throwable t) { return false; }
    }

    public static double getReach() { return isActive() ? reach : 3.0; }
    public static void setReach(double v) { reach = Math.max(3.0, Math.min(6.0, v)); }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
