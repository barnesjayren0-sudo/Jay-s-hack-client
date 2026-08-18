package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerBot extends Module {

    private long lastAttack = 0;
    private int nextDelay = 560;

    public TriggerBot() {
        super("TriggerBot", "Hits only on crosshair — preferred over aura", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null) return;
        if (Humanizer.shouldSkipTick(5)) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity player)) return;
        if (player == mc.player || !player.isAlive()) return;
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(player.getName().getString())) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;

        // miss chance — humans don't 100% connect
        if (Humanizer.chance(4)) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        mc.interactionManager.attackEntity(mc.player, player);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }
}
