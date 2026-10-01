package com.jay.hackclient.util;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {

    private MathUtil() {}

    public static int randomDelay(int min, int max) {
        if (max <= min) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    /** Plain clamp used by GUI + modules. */
    public static int clamp(int v, int min, int max) {
        return v < min ? min : Math.min(v, max);
    }

    public static double clamp(double v, double min, double max) {
        return v < min ? min : Math.min(v, max);
    }

    public static float clamp(float v, float min, float max) {
        return v < min ? min : Math.min(v, max);
    }

    /** Smooth 0..1 ramp for UI motion. */
    public static float easeLerp(float from, float to, float t) {
        return from + (to - from) * Math.max(0f, Math.min(1f, t));
    }

    public static float lerp(float from, float to, float t) {
        // shortest-path yaw lerp for large deltas
        float d = to - from;
        while (d < -180f) d += 360f;
        while (d > 180f) d -= 360f;
        return from + d * t;
    }

    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
