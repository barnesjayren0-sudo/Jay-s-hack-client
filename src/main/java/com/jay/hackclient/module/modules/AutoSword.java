package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoSword extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoSword() {
        super("AutoSword", "Automatically switches to the best sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        PlayerInventory inv = mc.player.getInventory();
        int bestSlot = -1;
        float bestDamage = 0;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.getItem() instanceof SwordItem sword) {
                // Simple damage check (can be improved with attributes later)
                float damage = 4.0f; // base, real calculation can be added
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }

        if (bestSlot != -1 && inv.selectedSlot != bestSlot) {
            inv.selectedSlot = bestSlot;
        }
    }
}
