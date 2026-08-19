package com.jay.hackclient.settings;

/**
 * Global tunable settings — quieter defaults for anti-detect.
 * Change via .jay set <name> <value>
 */
public final class ClientSettings {

    private ClientSettings() {}

    // Combat feel
    public static double aimRange = 4.5;
    public static float aimFov = 70f;
    public static float aimSmooth = 0.28f;
    public static double auraRange = 3.3;
    public static double hitboxExpand = 0.10;

    // Timing (ms) — wider = more human
    public static int combatDelayMin = 500;
    public static int combatDelayMax = 760;
    public static int clickDelayMin = 100;
    public static int clickDelayMax = 160;

    // Legit flags
    public static boolean requireAttackKey = true;  // AimAssist stronger only when clicking
    public static boolean cooldownCheck = true;     // wait for weapon cooldown
    public static int missChance = 5;               // % intentional miss
    public static int tickSkipChance = 7;           // % skip tick
    public static double velocityFactor = 0.75;     // not 0

    // Mode labels
    public static String mode = "sword"; // sword | nethpot | legit | rage

    public static void applySwordConfig() {
        mode = "sword";
        aimRange = 4.5;
        aimFov = 68f;
        aimSmooth = 0.30f;
        auraRange = 3.3;
        hitboxExpand = 0.10;
        combatDelayMin = 520;
        combatDelayMax = 740;
        requireAttackKey = true;
        cooldownCheck = true;
        missChance = 5;
        tickSkipChance = 8;
        velocityFactor = 0.74;
    }

    public static void applyNethpotConfig() {
        mode = "nethpot";
        aimRange = 4.2;
        aimFov = 75f;
        aimSmooth = 0.26f;
        auraRange = 3.2;
        hitboxExpand = 0.12;
        combatDelayMin = 480;
        combatDelayMax = 700;
        requireAttackKey = false;
        cooldownCheck = true;
        missChance = 4;
        tickSkipChance = 6;
        velocityFactor = 0.70;
    }

    public static void applyLegitConfig() {
        mode = "legit";
        aimRange = 4.0;
        aimFov = 55f;
        aimSmooth = 0.20f;
        auraRange = 3.1;
        hitboxExpand = 0.06;
        combatDelayMin = 560;
        combatDelayMax = 820;
        requireAttackKey = true;
        cooldownCheck = true;
        missChance = 8;
        tickSkipChance = 12;
        velocityFactor = 0.82;
    }

    public static void applyRageConfig() {
        mode = "rage";
        aimRange = 5.5;
        aimFov = 120f;
        aimSmooth = 0.55f;
        auraRange = 4.0;
        hitboxExpand = 0.28;
        combatDelayMin = 400;
        combatDelayMax = 560;
        requireAttackKey = false;
        cooldownCheck = false;
        missChance = 1;
        tickSkipChance = 2;
        velocityFactor = 0.55;
    }

    public static String summarize() {
        return String.format(
                "mode=%s aim=%.1f fov=%.0f smooth=%.2f aura=%.1f hb=%.2f delay=%d-%d vel=%.2f",
                mode, aimRange, aimFov, aimSmooth, auraRange, hitboxExpand,
                combatDelayMin, combatDelayMax, velocityFactor);
    }
}
