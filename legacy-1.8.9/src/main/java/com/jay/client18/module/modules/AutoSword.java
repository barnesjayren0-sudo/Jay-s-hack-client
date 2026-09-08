package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.Humanizer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

/** Swap only when needed + delayed — less hotbar flicker. */
public class AutoSword extends Module {

    private long last;
    private int nextDelay = 180;

    public AutoSword() {
        super("AutoSword", "Quiet best-sword swap", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        long now = System.currentTimeMillis();
        if (now - last < nextDelay) return;

        ItemStack held = mc.thePlayer.getHeldItem();
        if (held != null && held.getItem() instanceof ItemSword) {
            // already sword — only upgrade if clearly better
        }

        int best = -1;
        float bestDmg = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.thePlayer.inventory.getStackInSlot(i);
            if (s == null || !(s.getItem() instanceof ItemSword)) continue;
            float dmg = ((ItemSword) s.getItem()).getDamageVsEntity();
            if (dmg > bestDmg) {
                bestDmg = dmg;
                best = i;
            }
        }
        if (best >= 0 && mc.thePlayer.inventory.currentItem != best) {
            mc.thePlayer.inventory.currentItem = best;
            last = now;
            nextDelay = Humanizer.delay(160, 40);
        }
    }
}
