package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Quiet best-weapon swap (LB AutoWeapon concept, delayed + slot lock).
 */
public class AutoSword extends Module {

    public final BoolSetting onlyInCombat = new BoolSetting("OnlyCombat", "Only while in combat", true);

    private long last;
    private int nextDelay = 180;

    public AutoSword() {
        super("AutoSword", "Quiet best hotbar sword", Category.COMBAT);
        addSetting(onlyInCombat);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (onlyInCombat.get() && !CombatManager.isDuringCombat()) return;
        if (!SlotLock.tryAcquire("AutoSword", SlotLock.PRIO_SWORD)) return;

        long now = System.currentTimeMillis();
        if (now - last < nextDelay) return;

        int best = -1;
        float bestScore = -1f;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            float score = scoreWeapon(s);
            if (score > bestScore) {
                bestScore = score;
                best = i;
            }
        }

        if (best >= 0 && mc.player.getInventory().selectedSlot != best) {
            // Prefer accessor path used elsewhere if present
            try {
                mc.player.getInventory().selectedSlot = best;
            } catch (Throwable t) {
                try {
                    // Fabric 1.21 may need slot sync via interactionManager
                    mc.player.getInventory().setSelectedSlot(best);
                } catch (Throwable ignored) {}
            }
            last = now;
            nextDelay = Humanizer.swapDelay();
        }
    }

    private float scoreWeapon(ItemStack s) {
        Item it = s.getItem();
        // Prefer swords over axes slightly for 1.9+ PvP default
        if (it == Items.NETHERITE_SWORD) return 20;
        if (it == Items.DIAMOND_SWORD) return 18;
        if (it == Items.IRON_SWORD) return 14;
        if (it == Items.STONE_SWORD) return 10;
        if (it == Items.GOLDEN_SWORD) return 8;
        if (it == Items.WOODEN_SWORD) return 6;
        if (it == Items.NETHERITE_AXE) return 17;
        if (it == Items.DIAMOND_AXE) return 15;
        if (it == Items.IRON_AXE) return 12;
        return -1;
    }
}
