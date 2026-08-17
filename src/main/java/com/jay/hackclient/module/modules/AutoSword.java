package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;

public class AutoSword extends Module {

    public AutoSword() {
        super("AutoSword", "Switches to best hotbar sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        PlayerInventory inv = mc.player.getInventory();
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!(stack.getItem() instanceof SwordItem)) continue;

            // Prefer higher max damage via item name tier heuristic + durability left
            int score = scoreSword(stack);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0 && inv.selectedSlot != bestSlot) {
            inv.selectedSlot = bestSlot;
        }
    }

    private int scoreSword(ItemStack stack) {
        String id = stack.getItem().toString().toLowerCase();
        int tier = 1;
        if (id.contains("netherite")) tier = 6;
        else if (id.contains("diamond")) tier = 5;
        else if (id.contains("iron")) tier = 4;
        else if (id.contains("stone")) tier = 3;
        else if (id.contains("wooden") || id.contains("wood")) tier = 2;
        else if (id.contains("gold")) tier = 2;
        return tier * 1000 + stack.getMaxDamage() - stack.getDamage();
    }
}
