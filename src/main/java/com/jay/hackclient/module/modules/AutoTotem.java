package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Puts a totem in offhand when empty — core SMP kit survival. */
public class AutoTotem extends Module {

    private long lastSwap = 0;

    public AutoTotem() {
        super("AutoTotem", "Restocks offhand totem", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) return;

        long now = System.currentTimeMillis();
        if (now - lastSwap < MathUtil.randomDelay(80, 180)) return;

        int slot = findTotemSlot();
        if (slot == -1) return;

        // Inventory click: slot -> offhand (40)
        // Player screen slots: 9-35 main, 0-8 hotbar in sync id 0
        try {
            int syncId = mc.player.playerScreenHandler.syncId;
            // pick up totem
            mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            // place in offhand (slot 45 in player inventory handler)
            mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
            // if cursor still has item, put back
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            }
            lastSwap = now;
        } catch (Exception ignored) {
        }
    }

    private int findTotemSlot() {
        PlayerInventory inv = mc.player.getInventory();
        // Hotbar first (faster)
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return 36 + i; // conversion to screen slot
            }
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                return i;
            }
        }
        return -1;
    }
}
