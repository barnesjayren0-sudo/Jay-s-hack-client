package com.jay.hackclient.util;

import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;

import java.util.concurrent.ThreadLocalRandom;

public final class Humanizer {

    private Humanizer() {}

    private static final ThreadLocalRandom R = ThreadLocalRandom.current();

    public static int delay(int meanMs, int stdMs, int minMs, int maxMs) {
        double g = R.nextGaussian() * stdMs + meanMs;
        int v = (int) Math.round(g);
        if (v < minMs) v = minMs;
        if (v > maxMs) v = maxMs;
        if (R.nextInt(100) < 10) v += R.nextInt(50, 140);
        return scaleByPing(v);
    }

    private static int scaleByPing(int ms) {
        if (!ClientSettings.pingScaleDelays) return ms;
        int ping = getPing();
        if (ping <= 50) return ms;
        // add ~15% of ping above 50
        int extra = (int) ((ping - 50) * 0.15);
        return ms + Math.min(extra, 120);
    }

    public static int getPing() {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.getNetworkHandler() == null) return 0;
            var entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            return entry != null ? entry.getLatency() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int combatDelay() {
        int min = ClientSettings.combatDelayMin;
        int max = ClientSettings.combatDelayMax;
        int mean = (min + max) / 2;
        int std = Math.max(20, (max - min) / 3);
        return delay(mean, std, min, max);
    }

    public static int clickDelay() {
        return delay(
                (ClientSettings.clickDelayMin + ClientSettings.clickDelayMax) / 2,
                18, ClientSettings.clickDelayMin, ClientSettings.clickDelayMax);
    }

    public static int swapDelay() {
        return delay(100, 30, 55, 200);
    }

    public static float aimJitter() {
        return (float) (R.nextGaussian() * 0.4);
    }

    public static float aimSmooth(float base) {
        float n = base + (float) (R.nextGaussian() * 0.035);
        if (n < 0.10f) n = 0.10f;
        if (n > 0.85f) n = 0.85f;
        return n;
    }

    public static boolean chance(int percent) {
        return R.nextInt(100) < percent;
    }

    public static boolean shouldSkipTick() {
        return chance(ClientSettings.tickSkipChance);
    }

    public static boolean shouldSkipTick(int percent) {
        return chance(percent);
    }

    public static boolean shouldMiss() {
        return chance(ClientSettings.missChance);
    }
}
