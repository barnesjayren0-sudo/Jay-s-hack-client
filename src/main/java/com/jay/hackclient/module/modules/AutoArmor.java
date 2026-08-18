package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.item.ItemStack;

public class AutoArmor extends Module {

    private long lastScan = 0;

    public AutoArmor() {
        super("AutoArmor", "Detects armor in inventory", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (System.currentTimeMillis() - lastScan < 1000) return;
        lastScan = System.currentTimeMillis();

        // Full auto-equip needs container clicks; keep lightweight scan only
        for (int i = 0; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (ItemUtil.isArmor(stack)) {
                break;
            }
        }
    }
}
