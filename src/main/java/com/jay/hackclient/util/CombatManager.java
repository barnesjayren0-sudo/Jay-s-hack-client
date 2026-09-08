package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;

/**
 * Combat state + pause windows (inspired by LB CombatManager tick pauses).
 * pauseCombat / pauseRotation counted in ticks (20/s).
 */
public final class CombatManager {

    private static long lastHurtMs = 0;
    private static long lastAttackMs = 0;

    private static int pauseCombatTicks = 0;
    private static int pauseRotationTicks = 0;
    private static int duringCombatTicks = 0;

    public static final int DEFAULT_PAUSE_COMBAT = 40; // ~2s

    private CombatManager() {}

    public static void onHurt() {
        lastHurtMs = System.currentTimeMillis();
        duringCombatTicks = Math.max(duringCombatTicks, 60);
    }

    public static void onAttack() {
        lastAttackMs = System.currentTimeMillis();
        duringCombatTicks = Math.max(duringCombatTicks, 40);
    }

    public static void pauseCombat(int ticks) {
        pauseCombatTicks = Math.max(pauseCombatTicks, ticks);
    }

    public static void pauseRotation(int ticks) {
        pauseRotationTicks = Math.max(pauseRotationTicks, ticks);
    }

    public static void pause(long ms) {
        pauseCombat((int) Math.max(1, ms / 50));
    }

    public static boolean isCombatPaused() {
        return pauseCombatTicks > 0;
    }

    public static boolean isRotationPaused() {
        return pauseRotationTicks > 0;
    }

    public static boolean isPaused() {
        return isCombatPaused();
    }

    public static boolean isDuringCombat() {
        return duringCombatTicks > 0 || isInCombat(3000);
    }

    public static boolean isInCombat(long windowMs) {
        long now = System.currentTimeMillis();
        return (now - lastHurtMs) < windowMs || (now - lastAttackMs) < windowMs;
    }

    public static boolean canCombatModulesRun() {
        if (isCombatPaused()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;
        if (mc.currentScreen != null) return false;
        if (mc.player.isSpectator()) return false;
        return true;
    }

    public static boolean canRotate() {
        return !isRotationPaused() && canCombatModulesRun();
    }

    public static void tick() {
        if (pauseCombatTicks > 0) pauseCombatTicks--;
        if (pauseRotationTicks > 0) pauseRotationTicks--;
        if (duringCombatTicks > 0) duringCombatTicks--;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hurtTime == 9) {
            onHurt();
        }
    }
}
