package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Puts gap in offhand when healthy enough / not needing totem — kit PvP. */
public class OffhandGap extends Module {

    private long last = 0;
    private final float minHealthForGap = 14.0f;

    public OffhandGap() {
        super("OffhandGap", "Offhand gap when HP is safe", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        ItemStack off = mc.player.getOffHandStack();

        // Prefer totem if low
        if (hp < minHealthForGap) {
            if (!off.isOf(Items.TOTEM_OF_UNDYING)) {
                // let AutoTotem handle
            }
            return;
        }

        if (off.isOf(Items.GOLDEN_APPLE) || off.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return;

        long now = System.currentTimeMillis();
        if (now - last < MathUtil.randomDelay(100, 220)) return;

        int slot = findGap();
        if (slot == -1) return;

        try {
            int sync = mc.player.playerScreenHandler.syncId;
            mc.interactionManager.clickSlot(sync, slot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(sync, 45, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(sync, slot, 0, SlotActionType.PICKUP, mc.player);
            }
            last = now;
        } catch (Exception ignored) {
        }
    }

    private int findGap() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return 36 + i;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
        }
        return -1;
    }
}
