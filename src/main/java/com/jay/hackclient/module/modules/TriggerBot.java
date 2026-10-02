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

/** TriggerBot — hits when crosshair is on target. Real packets, public setTag. */
public class TriggerBot extends Module {

    public final NumberSetting minCooldown = new NumberSetting("Cooldown", "Min attack cooldown 0-1", 0.9, 0.5, 1.0, 0.05);
    public final NumberSetting hitChance = new NumberSetting("Hit Chance", "Percent of valid crosshair hits taken", 92, 40, 100, 1);
    public final BoolSetting playersOnly = new BoolSetting("Players Only", "Only players", true);
    public final BoolSetting weaponOnly = new BoolSetting("Weapons Only", "Sword/axe/mace only", true);
    public final BoolSetting visibleOnly = new BoolSetting("Visible Only", "Skip targets without line of sight", true);
    public final BoolSetting comboHit = new BoolSetting("Combo Hit", "Respect ComboHit gate", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla attack packets", true);

    private long lastAttack;
    private int nextDelay = 100;

    public TriggerBot() {
        super("TriggerBot", "Hit when crosshair is on target", Category.COMBAT);
        addSetting(minCooldown); addSetting(hitChance); addSetting(playersOnly); addSetting(weaponOnly);
        addSetting(visibleOnly); addSetting(comboHit); addSetting(realPackets);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        try { if (!CombatManager.canCombatModulesRun()) { setTag(null); return; } } catch (Throwable ignored) {}
        if (mc.player.isUsingItem()) return;
        if (weaponOnly.get()) {
            String n = mc.player.getMainHandStack().getItem().toString().toLowerCase();
            if (!n.contains("sword") && !n.contains("axe") && !n.contains("mace")) { setTag(null); return; }
        }
        try { if (Humanizer.shouldSkipTick()) return; } catch (Throwable ignored) {}
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) { setTag(null); return; }
        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (entity == mc.player) return;
        if (playersOnly.get()) {
            if (!(entity instanceof PlayerEntity player)) return;
            if (!player.isAlive()) return;
            try { if (AntiBot.isBot(player)) return; } catch (Throwable ignored) {}
            try {
                if (JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(player.getName().getString())) return;
            } catch (Throwable ignored) {}
        }
        if (visibleOnly.get() && !mc.player.canSee(entity)) return;
        if (comboHit.get() && entity instanceof PlayerEntity p
                && !ComboHit.shouldAttack(mc.player, p)) return;
        if (mc.player.getAttackCooldownProgress(0.5f) < minCooldown.getFloat()) return;
        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;
        try {
            // Humanized miss chance — occasionally holding fire reads as natural
            if (Humanizer.chance(100 - hitChance.getInt()) || Humanizer.shouldMiss()) {
                lastAttack = now;
                nextDelay = Humanizer.delay(140, 45, 70, 320);
                return;
            }
        } catch (Throwable ignored) {}
        if (realPackets.get()) {
            RealPackets.attackEntity(entity);
        } else if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        try { CombatManager.onAttack(); } catch (Throwable ignored) {}
        try { ReachHUD.recordHit(mc.player.distanceTo(entity)); } catch (Throwable ignored) {}
        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
        setTag(String.format("%.1f", mc.player.distanceTo(entity)));
    }
}
