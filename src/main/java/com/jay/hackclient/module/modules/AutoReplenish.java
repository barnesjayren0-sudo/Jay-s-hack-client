package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

/** Refill hotbar stacks from inventory when low (Meteor AutoReplenish-style, original). */
public class AutoReplenish extends Module {

    public final NumberSetting threshold = new NumberSetting("Threshold", "Refill under this count", 16, 1, 64, 1);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between moves", 120, 20, 400, 10);

    private long last;

    public AutoReplenish() {
        super("AutoReplenish", "Refill hotbar from inventory", Category.PLAYER);
        addSetting(threshold);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - last < delay.getInt()) return;

        for (int hot = 0; hot < 9; hot++) {
            ItemStack stack = mc.player.getInventory().getStack(hot);
            if (stack.isEmpty()) continue;
            if (stack.getCount() >= threshold.getInt()) continue;
            if (!stack.isStackable()) continue;

            Item item = stack.getItem();
            int inv = findInv(item, hot);
            if (inv < 0) continue;

            // click inv stack onto hotbar slot (player screen handler)
            try {
                int sync = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync, inv, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, 36 + hot, 0, SlotActionType.PICKUP, mc.player);
                // put remainder back
                mc.interactionManager.clickSlot(sync, inv, 0, SlotActionType.PICKUP, mc.player);
                last = now;
                return;
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    private int findInv(Item item, int avoidHot) {
        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (s.getItem() == item) return i;
        }
        return -1;
    }
}
