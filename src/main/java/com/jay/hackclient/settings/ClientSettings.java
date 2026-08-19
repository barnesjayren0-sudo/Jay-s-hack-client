package com.jay.hackclient.settings;

public final class ClientSettings {

    private ClientSettings() {}

    public static String aimMode = "classic";
    public static String targetPriority = "crosshair";

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

    public static double velocityHorizontal = 0.45;
    public static double velocityVertical = 0.90;
    public static String velocityMode = "medium";

    public static boolean pingScaleDelays = true;
    public static boolean hideHudOnScreenshot = true;
    public static boolean hideHudInDebug = true;

    public static String mode = "sword";

    public static void applyVelocityMode(String m) {
        velocityMode = m == null ? "medium" : m.toLowerCase();
        switch (velocityMode) {
            case "soft" -> { velocityHorizontal = 0.70; velocityVertical = 0.95; }
            case "strong" -> { velocityHorizontal = 0.32; velocityVertical = 0.85; }
            default -> {
                velocityMode = "medium";
                velocityHorizontal = 0.45;
                velocityVertical = 0.90;
            }
        }
    }

    /**
     * Sword PvP 1.9+ tuned config.
     * - Delays sit around full sword cooldown (~560–700ms)
     * - Classic aim, crosshair priority (no 360 aura feel)
     * - Medium velocity + small hitbox
     * - No KillAura in sword profile modules
     */
    public static void applySwordConfig() {
        mode = "sword";

        // Aim — controllable classic assist
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 4.25;
        aimFov = 72f;
        aimSmooth = 0.34f;          // stronger pull than before for sword trades

        // Never use KillAura range high in sword mode
        auraRange = 3.2;

        // Tiny expand — helps trades without blatant boxes
        hitboxExpand = 0.11;

        // Sword cooldown-ish window (1.9 combat)
        combatDelayMin = 540;
        combatDelayMax = 700;
        clickDelayMin = 105;
        clickDelayMax = 155;

        requireAttackKey = true;    // assist stronger when clicking
        cooldownCheck = true;       // TriggerBot waits for cooldown
        missChance = 4;
        tickSkipChance = 6;

        // Stick in trades without 0% KB flag
        applyVelocityMode("medium");
        velocityHorizontal = 0.42;
        velocityVertical = 0.92;

        pingScaleDelays = true;
    }

    /** Aggressive sword — still no full rage */
    public static void applySwordAggressiveConfig() {
        applySwordConfig();
        mode = "sword-aggro";
        aimSmooth = 0.40f;
        aimFov = 80f;
        aimRange = 4.5;
        hitboxExpand = 0.14;
        combatDelayMin = 500;
        combatDelayMax = 660;
        missChance = 2;
        velocityHorizontal = 0.36;
        velocityVertical = 0.88;
        velocityMode = "strong";
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
        applyVelocityMode("medium");
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
        applyVelocityMode("soft");
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
        applyVelocityMode("strong");
    }

    public static String summarize() {
        return String.format(
                "mode=%s aim=%s/%.0f/%.2f vel=%s h=%.2f delay=%d-%d",
                mode, aimMode, aimFov, aimSmooth, velocityMode,
                velocityHorizontal, combatDelayMin, combatDelayMax);
    }
}
