package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public class AutoSword extends Module {

    private long last;

    public AutoSword() {
        super("AutoSword", "Best hotbar sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        long now = System.currentTimeMillis();
        if (now - last < 150) return;

        int best = -1;
        int bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s == null || !(s.getItem() instanceof ItemSword)) continue;
            int score = s.getMaxDamage() - s.getItemDamage();
            // prefer higher material via max damage-ish + damage vs entity
            score += (int) ((ItemSword) s.getItem()).getDamageVsEntity();
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }
        if (best >= 0 && mc.thePlayer.inventory.currentItem != best) {
            mc.thePlayer.inventory.currentItem = best;
            last = now;
        }
    }
}
