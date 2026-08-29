package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Field;

/** Reduce right-click delay for blocks. */
public class FastPlace extends Module {

    public final NumberSetting delay = new NumberSetting("Delay", "Ticks between places", 0, 0, 4, 1);
    public final NumberSetting blocksOnly = new NumberSetting("BlocksOnly", "1=blocks only", 1, 0, 1, 1);

    private static Field cooldownField;

    public FastPlace() {
        super("FastPlace", "Faster block place", Category.PLAYER);
        addSetting(delay);
        addSetting(blocksOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        ItemStack stack = mc.player.getMainHandStack();
        if (blocksOnly.getInt() == 1 && !(stack.getItem() instanceof BlockItem)) return;

        try {
            if (cooldownField == null) {
                for (String name : new String[]{"itemUseCooldown", "field_1752", "f_91080_"}) {
                    try {
                        Field f = mc.getClass().getDeclaredField(name);
                        f.setAccessible(true);
                        cooldownField = f;
                        break;
                    } catch (NoSuchFieldException ignored) {}
                }
            }
            if (cooldownField != null) {
                int cur = cooldownField.getInt(mc);
                int d = delay.getInt();
                if (cur > d) cooldownField.setInt(mc, d);
            }
        } catch (Throwable ignored) {}
    }
}
