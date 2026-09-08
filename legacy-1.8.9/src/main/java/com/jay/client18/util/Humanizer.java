package com.jay.client18.util;

import java.util.Random;

/** Shared delays / jitter for ghost-style modules. */
public final class Humanizer {

    private static final Random R = new Random();

    private Humanizer() {}

    public static int delay(int mean, int spread) {
        int v = mean + (int) (R.nextGaussian() * spread);
        return Math.max(35, v);
    }

    public static int combatDelay() {
        // ~8–12 AP S feel for 1.8.9, not robotic
        return delay(95, 28);
    }

    public static int clickDelay(int minCps, int maxCps) {
        int cps = minCps + R.nextInt(Math.max(1, maxCps - minCps + 1));
        int ms = 1000 / Math.max(1, cps);
        return ms + R.nextInt(18);
    }

    public static float aimJitter() {
        return (float) (R.nextGaussian() * 0.35);
    }

    public static float soft(float base) {
        float n = base + (float) (R.nextGaussian() * 0.03);
        if (n < 0.08f) n = 0.08f;
        if (n > 0.45f) n = 0.45f;
        return n;
    }

    public static boolean chance(int percent) {
        return R.nextInt(100) < percent;
    }

    /** Skip some ticks so aim/trigger isn't every frame. */
    public static boolean shouldSkipTick() {
        return chance(12);
    }

    public static boolean shouldMiss() {
        return chance(4);
    }
}
