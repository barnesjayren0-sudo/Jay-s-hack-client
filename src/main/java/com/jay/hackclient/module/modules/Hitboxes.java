package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;

/** Tiny expand only — large values are obvious. */
public class Hitboxes extends Module {

    private static double expand = 0.04;

    public final NumberSetting size = new NumberSetting("Expand", "Extra radius", 0.04, 0.0, 0.20, 0.01);

    public Hitboxes() {
        super("Hitboxes", "Tiny hitbox expand (ghost)", Category.COMBAT);
        addSetting(size);
    }

    @Override
    public void onTick() {
        expand = Math.min(0.20, size.get());
        ClientSettings.hitboxExpand = expand;
    }

    @Override
    public void onDisable() {
        expand = 0.0;
    }

    public static double getExpand() {
        Module m = null;
        try {
            if (com.jay.hackclient.JayHackClient.moduleManager != null) {
                m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Hitboxes");
            }
        } catch (Throwable ignored) {}
        if (m == null || !m.isEnabled()) return 0.0;
        return expand;
    }

    public static void setExpand(double v) {
        expand = Math.max(0.0, Math.min(0.25, v));
    }
}
