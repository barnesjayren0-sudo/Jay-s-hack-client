package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.SlotLock;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class AutoSword extends Module {

    private long last;
    private int nextDelay = 150;

    public AutoSword() {
        super("AutoSword", "Best hotbar sword", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.delay(150, 28, 90, 240);
        last = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (SlotLock.isLockedByOther("AutoSword")) return;

        long now = System.currentTimeMillis();
        if (now - last < nextDelay) return;

        PlayerInventory inv = mc.player.getInventory();
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!ItemUtil.isSword(stack)) continue;
            int score = ItemUtil.swordTier(stack) * 1000 + (stack.getMaxDamage() - stack.getDamage());
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0 && inv.getSelectedSlot() != bestSlot) {
            if (SlotLock.tryAcquire("AutoSword", 200)) {
                inv.setSelectedSlot(bestSlot);
                last = now;
                nextDelay = Humanizer.delay(150, 28, 90, 240);
            }
        }
    }
}
