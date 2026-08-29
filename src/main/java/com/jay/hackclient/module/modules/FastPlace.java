package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/** Reduce right-click delay for blocks / crystals. */
public class FastPlace extends Module {

    public final NumberSetting delay = new NumberSetting("Delay", "Ticks between places", 0, 0, 4, 1);
    public final NumberSetting blocksOnly = new NumberSetting("BlocksOnly", "1=blocks only", 1, 0, 1, 1);

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
            // Yarn 1.21: itemUseCooldown on MinecraftClient
            mc.itemUseCooldown = Math.min(mc.itemUseCooldown, delay.getInt());
        } catch (Throwable ignored) {}
    }
}
