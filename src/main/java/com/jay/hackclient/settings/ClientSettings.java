package com.jay.hackclient.settings;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Global combat / client settings.
 * Defaults are GHOST-oriented (soft values under typical AC thresholds).
 */
public final class ClientSettings {

    private ClientSettings() {}

    public static String aimMode = "classic";
    public static String targetPriority = "crosshair";

    // Ghost defaults — tight FOV, slow smooth, near-vanilla reach
    public static double aimRange = 3.85;
    public static float aimFov = 48f;
    public static float aimSmooth = 0.12f;
    public static float aimDeadzone = 2.2f;
    public static float aimMaxStep = 3.2f;
    public static double auraRange = 3.05;
    public static float auraFov = 50f;
    public static boolean auraMultiTarget = false;
    public static double hitboxExpand = 0.04;

    public static double reachDistance = 3.05;
    public static String reachMode = "soft";

    // Longer delays = less robotic
    public static int combatDelayMin = 580;
    public static int combatDelayMax = 780;
    public static int clickDelayMin = 115;
    public static int clickDelayMax = 175;

    public static boolean requireAttackKey = true;
    public static boolean cooldownCheck = true;
    public static boolean critTiming = false;
    public static int missChance = 7;
    public static int tickSkipChance = 10;

    // Never low horizontal — soft ghost velocity
    public static double velocityHorizontal = 0.68;
    public static double velocityVertical = 1.0;
    public static String velocityMode = "soft";
    public static boolean velocityOnlyWhenHurt = true;
    /** Random near-vanilla velocity ticks (%) */
    public static int velocityVanillaChance = 15;

    public static boolean pingScaleDelays = true;
    public static boolean hideHudOnScreenshot = true;
    public static boolean hideHudInDebug = true;

    public static int potSlotMin = 0;
    public static int potSlotMax = 2;

    public static int arrayListColor = 0x3DDCFF;
    public static boolean arrayListRainbow = false;

    public static boolean toggleSounds = true;
    public static boolean showActiveCount = true;

    public static final Set<String> favorites = new HashSet<>();

    public static boolean firstLaunchDone = false;
    public static float guiScale = 1.0f;

    public static String mode = "legit";
    public static String lastProfile = "legit";

    public static final String[] PROFILE_CYCLE = {
            "legit", "scout", "sword", "nethpot", "builder", "explore", "anarchy"
    };
    public static int profileCycleIndex = 0;

    public static void addFavorite(String name) {
        if (name != null) favorites.add(name.toLowerCase(Locale.ROOT));
    }

    public static void removeFavorite(String name) {
        if (name != null) favorites.remove(name.toLowerCase(Locale.ROOT));
    }

    public static boolean isFavorite(String name) {
        return name != null && favorites.contains(name.toLowerCase(Locale.ROOT));
    }

    public static void toggleFavorite(String name) {
        if (name == null) return;
        String k = name.toLowerCase(Locale.ROOT);
        if (favorites.contains(k)) favorites.remove(k);
        else favorites.add(k);
    }

    public static void setGuiScale(float s) {
        guiScale = Math.max(0.85f, Math.min(1.25f, s));
    }

    public static void applyVelocityMode(String mode) {
        velocityMode = mode == null ? "soft" : mode.toLowerCase(Locale.ROOT);
        switch (velocityMode) {
            case "medium" -> velocityHorizontal = 0.58;
            case "strong" -> velocityHorizontal = 0.50; // still not blatant
            default -> velocityHorizontal = 0.68;
        }
        velocityVertical = 1.0;
    }

    /** Startup / dual — ghost legit baseline. */
    public static void applyDualConfig() {
        applyLegitConfig();
        mode = "dual";
    }

    public static void applySwordConfig() {
        mode = "sword";
        lastProfile = "sword";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 3.9;
        aimFov = 52f;
        aimSmooth = 0.14f;
        aimDeadzone = 2.0f;
        aimMaxStep = 3.5f;
        auraRange = 3.08;
        auraFov = 55f;
        hitboxExpand = 0.05;
        reachDistance = 3.06;
        combatDelayMin = 560;
        combatDelayMax = 740;
        missChance = 6;
        tickSkipChance = 9;
        requireAttackKey = true;
        cooldownCheck = true;
        applyVelocityMode("soft");
    }

    public static void applySwordAggressiveConfig() {
        applySwordConfig();
        mode = "swordaggro";
        lastProfile = "swordaggro";
        aimFov = 58f;
        aimSmooth = 0.16f;
        auraRange = 3.12;
        reachDistance = 3.1;
        velocityHorizontal = 0.60;
        missChance = 5;
    }

    public static void applyUtilityConfig() {
        mode = "utility";
        lastProfile = "utility";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 3.7;
        aimFov = 45f;
        aimSmooth = 0.12f;
        auraRange = 3.05;
        hitboxExpand = 0.03;
        reachDistance = 3.04;
        applyVelocityMode("soft");
        requireAttackKey = true;
        cooldownCheck = true;
        missChance = 8;
        tickSkipChance = 12;
    }

    public static void applyLegitConfig() {
        mode = "legit";
        lastProfile = "legit";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 3.7;
        aimFov = 45f;
        aimSmooth = 0.11f;
        aimDeadzone = 2.4f;
        aimMaxStep = 2.8f;
        auraRange = 3.05;
        auraFov = 48f;
        auraMultiTarget = false;
        hitboxExpand = 0.03;
        reachDistance = 3.04;
        combatDelayMin = 600;
        combatDelayMax = 820;
        clickDelayMin = 120;
        clickDelayMax = 180;
        missChance = 8;
        tickSkipChance = 12;
        requireAttackKey = true;
        cooldownCheck = true;
        critTiming = false;
        applyVelocityMode("soft");
        velocityVanillaChance = 18;
        hideHudOnScreenshot = true;
        hideHudInDebug = true;
    }

    public static void applyNethpotConfig() {
        applySwordConfig();
        mode = "nethpot";
        lastProfile = "nethpot";
        aimRange = 3.85;
        auraRange = 3.1;
    }

    public static String summarize() {
        return String.format(
                Locale.ROOT,
                "mode=%s aim=%.2f/%.0f smooth=%.2f aura=%.2f reach=%.2f velH=%.2f miss=%d",
                mode, aimRange, aimFov, aimSmooth, auraRange, reachDistance,
                velocityHorizontal, missChance);
    }
}
