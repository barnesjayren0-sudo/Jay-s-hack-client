package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

/** Reduces right-click delay for placing blocks (QoL). */
public class FastPlace extends Module {

    public final NumberSetting delay = new NumberSetting("Delay", "Ticks between places", 0, 0, 4, 1);

    public FastPlace() {
        super("FastPlace", "Faster block place", Category.PLAYER);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc == null) return;
        try {
            // itemUseCooldown field on MinecraftClient — set low when holding use
            var field = mc.getClass().getDeclaredField("itemUseCooldown");
            field.setAccessible(true);
            int cur = field.getInt(mc);
            int want = delay.getInt();
            if (cur > want) field.setInt(mc, want);
        } catch (Throwable t) {
            try {
                // Yarn alternate name
                var field = mc.getClass().getDeclaredField("field_1752");
                field.setAccessible(true);
                int cur = field.getInt(mc);
                if (cur > delay.getInt()) field.setInt(mc, delay.getInt());
            } catch (Throwable ignored) {}
        }
    }
}
