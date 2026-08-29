package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Restock offhand totem — high priority when low HP. */
public class AutoTotem extends Module {

    public final NumberSetting softHp = new NumberSetting("SoftHP", "Faster under this HP", 12, 4, 20, 1);

    private long lastSwap;

    public AutoTotem() {
        super("AutoTotem", "Restock offhand totem", Category.PLAYER);
        addSetting(softHp);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("AutoTotem")) return;

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) return;

        long now = System.currentTimeMillis();
        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        int delay = hp <= softHp.getFloat() ? 40 : Humanizer.swapDelay();
        if (now - lastSwap < delay) return;

        int slot = findTotemSlot();
        if (slot == -1) return;

        // Priority 35 — beats most combat hotbar swaps when popping
        if (!SlotLock.tryAcquire("AutoTotem", 400, 35)) return;

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
