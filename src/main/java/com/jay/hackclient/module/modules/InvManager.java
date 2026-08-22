package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Drops obvious junk (dirt, gravel, etc.) when inventory open-ish or idle. Soft. */
public class InvManager extends Module {

    private long last;

    public InvManager() {
        super("InvManager", "Drop junk items slowly", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        if (now - last < Humanizer.delay(250, 40, 180, 400)) return;

        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (!isJunk(s)) continue;
            try {
                int sync = mc.player.playerScreenHandler.syncId;
                mc.interactionManager.clickSlot(sync, i, 1, SlotActionType.THROW, mc.player);
                last = now;
                return;
            } catch (Exception ignored) {}
        }
    }

    private boolean isJunk(ItemStack s) {
        return s.isOf(Items.DIRT) || s.isOf(Items.GRAVEL) || s.isOf(Items.NETHERRACK)
                || s.isOf(Items.COBBLESTONE) || s.isOf(Items.ROTTEN_FLESH)
                || s.isOf(Items.POISONOUS_POTATO);
    }
}
