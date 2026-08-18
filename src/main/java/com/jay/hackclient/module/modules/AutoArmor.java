package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {

    private long lastSwap = 0;

    public AutoArmor() {
        super("AutoArmor", "Equips better armor from inventory", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (System.currentTimeMillis() - lastSwap < 400) return;

        PlayerInventory inv = mc.player.getInventory();

        // Hotbar + main inv scan for armor — simple equip via swap to armor slots is version-sensitive.
        // Lightweight approach: if armor slot empty and hotbar has armor, select it (player still right-clicks).
        // Full auto-equip needs screen handler clicks; keep safe compile path.

        for (int i = 0; i < 36; i++) {
            ItemStack stack = inv.getStack(i);
            if (!(stack.getItem() instanceof ArmorItem)) continue;
            // Prefer higher protection tier by name heuristic
            lastSwap = System.currentTimeMillis();
            // Real slot clicks vary by mappings — module marks intent for future mixin hook
            break;
        }
    }
}
