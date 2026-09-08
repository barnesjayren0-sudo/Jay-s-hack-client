package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;

/**
 * Lightweight combat state (LB CombatManager idea).
 * Modules can pause when not "in combat" or when UI is open.
 */
public final class CombatManager {

    private static long lastHurtMs = 0;
    private static long lastAttackMs = 0;
    private static long pauseUntilMs = 0;

    private CombatManager() {}

    public static void onHurt() {
        lastHurtMs = System.currentTimeMillis();
    }

    public static void onAttack() {
        lastAttackMs = System.currentTimeMillis();
    }

    public static void pause(long ms) {
        pauseUntilMs = System.currentTimeMillis() + ms;
    }

    public static boolean isPaused() {
        return System.currentTimeMillis() < pauseUntilMs;
    }

    public static boolean isInCombat(long windowMs) {
        long now = System.currentTimeMillis();
        return (now - lastHurtMs) < windowMs || (now - lastAttackMs) < windowMs;
    }

    public static boolean canCombatModulesRun() {
        if (isPaused()) return false;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return false;
        if (mc.currentScreen != null) return false;
        if (mc.player.isSpectator()) return false;
        return true;
    }

    public static void tick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hurtTime == 9) {
            onHurt();
        }
    }
}
