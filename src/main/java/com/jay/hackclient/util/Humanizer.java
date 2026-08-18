package com.jay.hackclient.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Timing + aim noise so actions look less robotic.
 * Not a bypass — just reduces obvious patterns.
 */
public final class Humanizer {

    private Humanizer() {}

    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    /** Gaussian-ish delay around mean with stddev, clamped. */
    public static int delay(int meanMs, int stdMs, int minMs, int maxMs) {
        double g = R.nextGaussian() * stdMs + meanMs;
        int v = (int) Math.round(g);
        if (v < minMs) v = minMs;
        if (v > maxMs) v = maxMs;
        // occasional "hesitation"
        if (R.nextInt(100) < 8) {
            v += R.nextInt(40, 120);
        }
        return v;
    }

    public static int combatDelay() {
        return delay(560, 70, 450, 780);
    }

    public static int clickDelay() {
        return delay(125, 18, 95, 165);
    }

    public static int swapDelay() {
        return delay(90, 25, 50, 180);
    }

    /** Small random yaw/pitch noise in degrees. */
    public static float aimJitter() {
        return (float) (R.nextGaussian() * 0.35);
    }

    /** Soft lerp factor with noise. */
    public static float aimSmooth(float base) {
        float n = base + (float) (R.nextGaussian() * 0.04);
        if (n < 0.12f) n = 0.12f;
        if (n > 0.85f) n = 0.85f;
        return n;
    }

    public static boolean chance(int percent) {
        return R.nextInt(100) < percent;
    }

    /** Skip this tick sometimes to break perfect every-tick patterns. */
    public static boolean shouldSkipTick(int skipPercent) {
        return chance(skipPercent);
    }
}
