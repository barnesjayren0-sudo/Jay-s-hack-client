package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Raises shield when an enemy is close and you're not attacking. */
public class AutoBlock extends Module {

    public AutoBlock() {
        super("AutoBlock", "Auto shield when enemy close", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.options.attackKey.isPressed()) return;

        ItemStack off = mc.player.getOffHandStack();
        ItemStack main = mc.player.getMainHandStack();
        boolean hasShield = off.isOf(Items.SHIELD) || main.isOf(Items.SHIELD);
        if (!hasShield) return;

        boolean threat = false;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (AntiBot.isBot(p)) continue;
            if (mc.player.distanceTo(p) < 3.6) {
                threat = true;
                break;
            }
        }
        if (!threat) return;
        if (Humanizer.shouldSkipTick(10)) return;

        Hand hand = off.isOf(Items.SHIELD) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!mc.player.isUsingItem()) {
            mc.interactionManager.interactItem(mc.player, hand);
        }
    }
}
