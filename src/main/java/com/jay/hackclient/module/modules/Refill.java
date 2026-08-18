package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Refills hotbar pots / pearls / gaps from inventory — SMP kit support. */
public class Refill extends Module {

    private long last = 0;

    public Refill() {
        super("Refill", "Refills hotbar pots/pearls/gaps", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        long now = System.currentTimeMillis();
        if (now - last < MathUtil.randomDelay(200, 400)) return;

        // Find empty hotbar slot and item in inv to pull
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            ItemStack hb = mc.player.getInventory().getStack(hotbar);
            if (!hb.isEmpty()) continue;

            int invSlot = findRefillable();
            if (invSlot == -1) return;

            try {
                int sync = mc.player.playerScreenHandler.syncId;
                int hbScreen = 36 + hotbar;
                mc.interactionManager.clickSlot(sync, invSlot, 0, SlotActionType.QUICK_MOVE, mc.player);
                last = now;
                return;
            } catch (Exception ignored) {
            }
        }
    }

    private int findRefillable() {
        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            Item it = s.getItem();
            if (it == Items.END_CRYSTAL
                    || it == Items.ENDER_PEARL
                    || it == Items.GOLDEN_APPLE
                    || it == Items.ENCHANTED_GOLDEN_APPLE
                    || it == Items.SPLASH_POTION
                    || it == Items.TOTEM_OF_UNDYING
                    || it == Items.OBSIDIAN
                    || it == Items.RESPAWN_ANCHOR
                    || it == Items.GLOWSTONE) {
                return i;
            }
        }
        return -1;
    }
}
