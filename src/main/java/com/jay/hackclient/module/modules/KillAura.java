package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

public class KillAura extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private long lastAttack = 0;

    public KillAura() {
        super("KillAura", "Automatically attacks players with sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // Only work if holding a sword
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;

        LivingEntity target = null;
        double closest = 4.2; // Slightly extended reach for sword PvP

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player) continue;
            if (!player.isAlive()) continue;

            double dist = mc.player.distanceTo(player);
            if (dist < closest) {
                closest = dist;
                target = player;
            }
        }

        if (target != null && System.currentTimeMillis() - lastAttack > 500) { // ~1.8 CPS for sword
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = System.currentTimeMillis();
        }
    }
}
