package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

/** Soft hitbox expand for targeting (client-side). */
public class Hitboxes extends Module {

    private static double expand = 0.15;

    public final NumberSetting size = new NumberSetting("Expand", "Extra hit radius", 0.15, 0.0, 0.6, 0.05);

    public Hitboxes() {
        super("Hitboxes", "Slightly larger enemy hitboxes", Category.COMBAT);
        addSetting(size);
    }

    @Override
    public void onTick() {
        expand = size.get();
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
        expand = Math.max(0.0, Math.min(0.8, v));
    }
}
