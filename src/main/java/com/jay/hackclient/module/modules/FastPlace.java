package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

public class FastPlace extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", "Ticks between places", 0, 0, 4, 1);
    public FastPlace() { super("FastPlace", "Faster block / item place", Category.PLAYER); addSetting(delay); }
    @Override public void onTick() {
        if (mc.player == null) return;
        try {
            var f = mc.getClass().getDeclaredField("itemUseCooldown");
            f.setAccessible(true);
            int d = (int) delay.get();
            if ((int)f.get(mc) > d) f.set(mc, d);
        } catch (Throwable ignored) {}
        setTag(String.valueOf((int)delay.get()));
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
