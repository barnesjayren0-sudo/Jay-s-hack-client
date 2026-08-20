package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Nethpot-style splash heal when low HP. */
public class AutoPot extends Module {

    private long lastPot = 0;
    private final float healthThreshold = 12.0f; // 6 hearts

    public AutoPot() {
        super("AutoPot", "Throws splash heal pot when low HP", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("AutoPot")) return;
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > healthThreshold) return;
        if (mc.player.hasStatusEffect(StatusEffects.REGENERATION)
                && mc.player.getHealth() > 8) return;

        long now = System.currentTimeMillis();
        if (now - lastPot < MathUtil.randomDelay(650, 950)) return;

        int slot = findSplashHeal();
        if (slot == -1) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        if (!SlotLock.tryAcquire("AutoPot", 450)) return;
        mc.player.getInventory().setSelectedSlot(slot);

        // Look down slightly for self-pot
        float pitch = mc.player.getPitch();
        mc.player.setPitch(Math.min(90f, pitch + 60f));

        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.swingHand(Hand.MAIN_HAND);

        mc.player.setPitch(pitch);
        mc.player.getInventory().setSelectedSlot(prev);
        SlotLock.release("AutoPot");
        lastPot = now;
    }

    private int findSplashHeal() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
                // Prefer anything named heal/regeneration — name check is soft
                String n = stack.getName().getString().toLowerCase();
                if (n.contains("heal") || n.contains("regeneration") || n.contains("instant") || n.contains("potion")) {
                    return i;
                }
                return i; // fallback any splash
            }
        }
        return -1;
    }
}
