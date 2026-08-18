package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** UHC / pot PvP — eats gapple when low. */
public class AutoGap extends Module {

    private long lastEat = 0;
    private final float healthThreshold = 14.0f;

    public AutoGap() {
        super("AutoGap", "Eats golden apple when low HP", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.player.isUsingItem()) return;
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > healthThreshold) return;

        long now = System.currentTimeMillis();
        if (now - lastEat < MathUtil.randomDelay(800, 1200)) return;

        int slot = findGap();
        if (slot == -1) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        // keep selected while eating; restore next ticks if finished
        if (!mc.player.isUsingItem()) {
            mc.player.getInventory().setSelectedSlot(prev);
        }
        lastEat = now;
    }

    private int findGap() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
        }
        return -1;
    }
}
