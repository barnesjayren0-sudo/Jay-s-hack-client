package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** UHC / pot PvP — eats gapple when low. */
public class AutoGap extends Module {

    private long lastEat = 0;
    private int savedSlot = -1;
    private final float healthThreshold = 14.0f;

    public AutoGap() {
        super("AutoGap", "Eats golden apple when low HP", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.player.isUsingItem()) return;
        if (SlotLock.isLockedByOther("AutoGap")) return;
        if (savedSlot >= 0) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
            savedSlot = -1;
            SlotLock.release("AutoGap");
        }
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > healthThreshold) return;

        long now = System.currentTimeMillis();
        if (now - lastEat < MathUtil.randomDelay(800, 1200)) return;

        int slot = findGap();
        if (slot == -1) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        if (!SlotLock.tryAcquire("AutoGap", 1800)) return;
        mc.player.getInventory().setSelectedSlot(slot);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        // Keep the consumable selected until vanilla finishes using it.
        if (mc.player.isUsingItem()) {
            savedSlot = prev;
        } else {
            mc.player.getInventory().setSelectedSlot(prev);
            SlotLock.release("AutoGap");
        }
        lastEat = now;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot >= 0) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        SlotLock.release("AutoGap");
    }

    private int findGap() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
        }
        return -1;
    }
}
