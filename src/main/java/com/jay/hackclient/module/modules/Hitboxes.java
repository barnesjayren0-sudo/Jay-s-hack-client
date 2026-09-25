package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

public class Hitboxes extends Module {
    private static double expand = 0.1;
    public final NumberSetting size = new NumberSetting("Expand", "Box expand", 0.1, 0.0, 1.0, 0.05);
    public Hitboxes() { super("Hitboxes", "Expand entity hitboxes", Category.COMBAT); addSetting(size); }
    @Override public void onTick() { expand = size.get(); setTag(String.format("+%.2f", expand)); }
    @Override public void onDisable() { expand = 0; setTag(null); }
    public static double getExpand() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Hitboxes");
            if (m != null && m.isEnabled()) return expand;
        } catch (Throwable ignored) {}
        return 0;
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
