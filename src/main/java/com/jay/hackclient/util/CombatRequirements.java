package com.jay.hackclient.util;

import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;

/**
 * Shared combat gates (inspired by LB KillAuraRequirements).
 * Click / weapon / not-breaking / cooldown.
 */
public final class CombatRequirements {

    private CombatRequirements() {}

    public static boolean clickHeldOrRecent(long recentMs) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return false;
        if (mc.options.attackKey.isPressed()) return true;
        // "recent" approximated via combat manager attack window
        return CombatManager.isInCombat(recentMs);
    }

    public static boolean requireClick() {
        if (!ClientSettings.requireAttackKey) return true;
        return clickHeldOrRecent(250);
    }

    public static boolean holdingWeapon() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        return ItemUtil.isSwordOrAxe(mc.player.getMainHandStack());
    }

    public static boolean notBreaking() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.interactionManager == null) return true;
        return !mc.interactionManager.isBreakingBlock();
    }

    public static boolean cooldownReady(float min) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        if (!ClientSettings.cooldownCheck) return true;
        return mc.player.getAttackCooldownProgress(0.5f) >= min;
    }

    /** All standard gates for ghost combat modules. */
    public static boolean standardCombat(boolean needWeapon, boolean needClick) {
        if (!CombatManager.canCombatModulesRun()) return false;
        if (needWeapon && !holdingWeapon()) return false;
        if (needClick && !requireClick()) return false;
        if (!notBreaking()) return false;
        return true;
    }

    /** LB-style: can we land a vanilla critical this tick? */
    public static boolean allowsCriticalHit() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;
        var p = mc.player;
        if (p.isOnGround() || p.isClimbing() || p.hasVehicle()) return false;
        if (p.isTouchingWater() || p.isInLava() || p.isInPowderSnow()) return false;
        if (p.getAbilities().flying) return false;
        return p.fallDistance > 0.0f;
    }
}
