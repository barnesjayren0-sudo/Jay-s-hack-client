package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {

    private long lastSwap = 0;

    public AutoTotem() {
        super("AutoTotem", "Restock offhand totem", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("AutoTotem")) return;

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) return;

        long now = System.currentTimeMillis();
        if (now - lastSwap < Humanizer.swapDelay()) return;

        int slot = findTotemSlot();
        if (slot == -1) return;
        if (!SlotLock.tryAcquire("AutoTotem", 350)) return;

        try {
            int syncId = mc.player.playerScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            }
            lastSwap = now;
        } catch (Exception ignored) {
        } finally {
            SlotLock.release("AutoTotem");
        }
    }

    private int findTotemSlot() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return 36 + i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
