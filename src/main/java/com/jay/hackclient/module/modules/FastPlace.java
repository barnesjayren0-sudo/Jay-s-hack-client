package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

/** FastPlace — faster block/item place. Public setTag, null-safe. */
public class FastPlace extends Module {
    // Vanilla item use cooldown is 4 ticks; legit default shaves only one tick off.
    public final NumberSetting delay = new NumberSetting("Delay", "Ticks between places", 3, 1, 4, 1);

    public FastPlace() { super("FastPlace", "Slightly faster block / item place", Category.PLAYER); addSetting(delay); }

    @Override public void onTick() {
        if (mc.player == null) { return; }
        try {
            var f = mc.getClass().getDeclaredField("itemUseCooldown");
            f.setAccessible(true);
            int d = Math.max(1, delay.getInt()); // never fully instant — that reads as automation
            if ((int) f.get(mc) > d) f.set(mc, d);
        } catch (Throwable ignored) {}
        setTag(String.valueOf(delay.getInt()));
    }
}
