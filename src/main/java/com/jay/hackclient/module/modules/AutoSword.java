package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

public class AutoSword extends Module {

    private long last;

    public AutoSword() {
        super("AutoSword", "Select best hotbar sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Don't fight ShieldBreak axe swaps
        Module sb = com.jay.hackclient.JayHackClient.moduleManager != null
                ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("ShieldBreak")
                : null;
        if (sb != null && sb.isEnabled()) {
            // allow axe if target blocking — ShieldBreak owns slot
            for (PlayerEntityProxy ignored : new PlayerEntityProxy[0]) {}
        }

        long now = System.currentTimeMillis();
        if (now - last < 150) return;

        // If currently holding axe and ShieldBreak on, skip
        if (sb != null && sb.isEnabled() && ItemUtil.isAxe(mc.player.getMainHandStack())) return;

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
            inv.setSelectedSlot(bestSlot);
            last = now;
        }
    }

    private static class PlayerEntityProxy {}
}
