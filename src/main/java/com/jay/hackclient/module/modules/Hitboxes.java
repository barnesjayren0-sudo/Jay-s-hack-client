package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

public class Hitboxes extends Module {
    private static double expand = 0.1;
    public final NumberSetting size = new NumberSetting("Expand", "Box expand", 0.1, 0.0, 1.0, 0.05);

    public Hitboxes() {
        super("Hitboxes", "Expand entity hitboxes", Category.COMBAT);
        addSetting(size);
    }

    @Override public void onTick() {
        expand = size.get();
        setTag(String.format("+%.2f", expand));
    }

    @Override public void onDisable() {
        expand = 0;
        setTag(null);
    }

    public static double getExpand() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Hitboxes");
            if (m != null && m.isEnabled()) return expand;
        } catch (Throwable ignored) {}
        return 0;
    }

    public static void setExpand(double v) {
        expand = Math.max(0, Math.min(1.0, v));
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Hitboxes");
            if (m instanceof Hitboxes h) h.size.set(expand);
        } catch (Throwable ignored) {}
    }

    public static boolean isActive() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Hitboxes");
            return m != null && m.isEnabled() && expand > 0.001;
        } catch (Throwable t) {
            return false;
        }
    }
}
