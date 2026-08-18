package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;

/** Detects low-res / phone-like windows for lighter work. */
public final class Mobile {

    private Mobile() {}

    public static boolean isSmallScreen() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return true;
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        return w < 500 || h < 320;
    }

    /** On phones, skip expensive work more often. */
    public static boolean shouldThrottle() {
        return isSmallScreen() && Humanizer.chance(20);
    }
}
