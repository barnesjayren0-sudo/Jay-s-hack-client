package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Refills splash pots into configured hotbar slot range. */
public class PotRefill extends Module {

    private long last;

    public PotRefill() {
        super("PotRefill", "Refill hotbar pots (nethpot)", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("PotRefill")) return;

        long now = System.currentTimeMillis();
        if (now - last < Humanizer.swapDelay() + 100) return;

        int min = Math.max(0, Math.min(8, ClientSettings.potSlotMin));
        int max = Math.max(min, Math.min(8, ClientSettings.potSlotMax));

        for (int hotbar = min; hotbar <= max; hotbar++) {
            ItemStack hb = mc.player.getInventory().getStack(hotbar);
            if (!hb.isEmpty() && (hb.isOf(Items.SPLASH_POTION) || hb.isOf(Items.LINGERING_POTION))) continue;
            if (!hb.isEmpty()) continue;

            int inv = findPotInInv();
            if (inv < 0) return;

            if (!SlotLock.tryAcquire("PotRefill", 200)) return;

            try {
                int sync = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync, inv, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, 36 + hotbar, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(sync, inv, 0, SlotActionType.PICKUP, mc.player);
                }
                last = now;
                return;
            } catch (Exception ignored) {
            } finally {
                SlotLock.release("PotRefill");
            }
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
