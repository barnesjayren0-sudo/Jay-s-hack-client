package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** TriggerBot — real packets only. Hits when crosshair is on target. */
public class TriggerBot extends Module {

    public final NumberSetting minCooldown = new NumberSetting("Cooldown", "Min attack cooldown 0-1", 0.9, 0.5, 1.0, 0.05);
    public final BoolSetting playersOnly = new BoolSetting("Players Only", "Only players", true);
    public final BoolSetting weaponOnly = new BoolSetting("Weapons Only", "Sword/axe only", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla attack packets", true);

    private long lastAttack;
    private int nextDelay = 100;

    public TriggerBot() {
        super("TriggerBot", "Hit when crosshair is on target", Category.COMBAT);
        addSetting(minCooldown); addSetting(playersOnly); addSetting(weaponOnly); addSetting(realPackets);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.player.isUsingItem()) return;
        if (weaponOnly.get()) {
            String n = mc.player.getMainHandStack().getItem().toString().toLowerCase();
            if (!n.contains("sword") && !n.contains("axe") && !n.contains("mace")) return;
        }
        try { if (Humanizer.shouldSkipTick()) return; } catch (Throwable ignored) {}
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (playersOnly.get()) {
            if (!(entity instanceof PlayerEntity player)) return;
            if (player == mc.player || !player.isAlive()) return;
            try { if (AntiBot.isBot(player)) return; } catch (Throwable ignored) {}
            try {
                if (JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(player.getName().getString())) return;
            } catch (Throwable ignored) {}
        }
        if (mc.player.getAttackCooldownProgress(0.5f) < minCooldown.getFloat()) return;
        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;
        try {
            if (Humanizer.shouldMiss()) {
                lastAttack = now; nextDelay = 80 + (int)(Math.random()*40); return;
            }
        } catch (Throwable ignored) {}
        if (realPackets.get()) RealPackets.attackEntity(entity);
        else if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        try { CombatManager.onAttack(); } catch (Throwable ignored) {}
        lastAttack = now; nextDelay = 80 + (int)(Math.random()*50);
        setTag(String.format("%.1f", mc.player.distanceTo(entity)));
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
