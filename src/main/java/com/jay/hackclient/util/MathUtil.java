package com.jay.hackclient.util;

import java.util.concurrent.ThreadLocalRandom;

public final class MathUtil {

    private MathUtil() {}

    public static int randomDelay(int minMs, int maxMs) {
        if (maxMs <= minMs) return minMs;
        return ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
    }

    public static double randomDouble(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
