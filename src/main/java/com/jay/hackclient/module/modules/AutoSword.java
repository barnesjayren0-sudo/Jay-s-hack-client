package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class AutoSword extends Module {

    private long last;

    public AutoSword() {
        super("AutoSword", "Best hotbar sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (SlotLock.isLockedByOther("AutoSword")) return;

        long now = System.currentTimeMillis();
        if (now - last < 150) return;

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
            }
        }
    }
}
