package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Sticky target lock (inspired by LB KillAuraTargetTracker concept).
 * Prevents aim from flicking between players every tick.
 */
public final class TargetTracker {

    private static int lockedId = -1;
    private static long lockUntilMs = 0;

    private TargetTracker() {}

    public static void lock(Entity e, long holdMs) {
        if (e == null) return;
        lockedId = e.getId();
        lockUntilMs = System.currentTimeMillis() + Math.max(50L, holdMs);
    }

    public static void clear() {
        lockedId = -1;
        lockUntilMs = 0;
    }

    public static boolean isLocked() {
        return lockedId != -1 && System.currentTimeMillis() < lockUntilMs;
    }

    public static PlayerEntity getLockedPlayer() {
        if (!isLocked()) return null;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.world == null) return null;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p.getId() == lockedId) return p;
        }
        clear();
        return null;
    }

    /**
     * Prefer locked target if still valid; else use candidate and lock it.
     */
    public static PlayerEntity prefer(PlayerEntity candidate, long holdMs) {
        PlayerEntity locked = getLockedPlayer();
        if (locked != null && locked.isAlive() && !locked.isSpectator()) {
            return locked;
        }
        if (candidate != null) {
            lock(candidate, holdMs);
        } else {
            clear();
        }
        return candidate;
    }
}
