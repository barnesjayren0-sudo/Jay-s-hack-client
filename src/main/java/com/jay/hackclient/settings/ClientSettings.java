package com.jay.hackclient.settings;

public final class ClientSettings {

    private ClientSettings() {}

    public static String aimMode = "classic"; // classic | silent
    public static String targetPriority = "crosshair"; // crosshair | closest | lowest_hp

    public static double aimRange = 4.5;
    public static float aimFov = 70f;
    public static float aimSmooth = 0.28f;
    public static double auraRange = 3.3;
    public static double hitboxExpand = 0.10;

    public static int combatDelayMin = 500;
    public static int combatDelayMax = 760;
    public static int clickDelayMin = 100;
    public static int clickDelayMax = 160;

    public static boolean requireAttackKey = true;
    public static boolean cooldownCheck = true;
    public static int missChance = 5;
    public static int tickSkipChance = 7;
    public static double velocityFactor = 0.72;

    public static boolean pingScaleDelays = true;
    public static boolean hideHudOnScreenshot = true;
    public static boolean hideHudInDebug = true; // F3

    public static String mode = "sword";

    public static void applySwordConfig() {
        mode = "sword";
        aimMode = "classic";
        targetPriority = "crosshair";
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
        aimMode = "classic";
        targetPriority = "lowest_hp";
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
        aimMode = "classic";
        targetPriority = "crosshair";
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
        aimMode = "silent";
        targetPriority = "closest";
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
                "mode=%s aim=%s prio=%s range=%.1f fov=%.0f smooth=%.2f vel=%.2f",
                mode, aimMode, targetPriority, aimRange, aimFov, aimSmooth, velocityFactor);
    }
}
