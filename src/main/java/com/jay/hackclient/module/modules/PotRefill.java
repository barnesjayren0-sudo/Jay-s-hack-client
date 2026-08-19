package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Keeps splash pots on hotbar slots 1-3 for nethpot. */
public class PotRefill extends Module {

    private long last;

    public PotRefill() {
        super("PotRefill", "Refill hotbar pots (nethpot)", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - last < Humanizer.swapDelay() + 100) return;

        for (int hotbar = 0; hotbar <= 2; hotbar++) {
            ItemStack hb = mc.player.getInventory().getStack(hotbar);
            if (!hb.isEmpty() && (hb.isOf(Items.SPLASH_POTION) || hb.isOf(Items.LINGERING_POTION))) continue;
            if (!hb.isEmpty()) continue;

            int inv = findPotInInv();
            if (inv < 0) return;

            try {
                int sync = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync, inv, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, 36 + hotbar, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(sync, inv, 0, SlotActionType.PICKUP, mc.player);
                }
                last = now;
                return;
            } catch (Exception ignored) {}
        }
    }

    private int findPotInInv() {
        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.SPLASH_POTION) || s.isOf(Items.LINGERING_POTION)) return i;
        }
        return -1;
    }
}
