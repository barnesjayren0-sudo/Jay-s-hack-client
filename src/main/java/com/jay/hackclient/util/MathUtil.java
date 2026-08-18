package com.jay.hackclient.util;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {

    private MathUtil() {}

    public static int randomDelay(int min, int max) {
        if (max <= min) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
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
