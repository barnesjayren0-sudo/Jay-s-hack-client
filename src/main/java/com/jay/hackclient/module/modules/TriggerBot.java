package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerBot extends Module {

    private long lastAttack = 0;
    private final int delayMs = 520;

    public TriggerBot() {
        super("TriggerBot", "Attacks when crosshair is on a player", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity player)) return;
        if (player == mc.player || !player.isAlive()) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < delayMs) return;

        mc.interactionManager.attackEntity(mc.player, player);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
    }
}
