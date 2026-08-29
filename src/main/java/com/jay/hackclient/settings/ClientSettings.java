package com.jay.hackclient.settings;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class ClientSettings {

    private ClientSettings() {}

    public static String aimMode = "classic";
    public static String targetPriority = "crosshair";

    public static double aimRange = 4.25;
    public static float aimFov = 70f;
    public static float aimSmooth = 0.22f;
    public static float aimDeadzone = 1.8f;
    public static float aimMaxStep = 4.5f;
    public static double auraRange = 3.2;
    public static float auraFov = 70f;
    public static boolean auraMultiTarget = false;
    public static double hitboxExpand = 0.10;

    public static double reachDistance = 3.12;
    public static String reachMode = "soft";

    public static int combatDelayMin = 540;
    public static int combatDelayMax = 700;
    public static int clickDelayMin = 105;
    public static int clickDelayMax = 155;

    public static boolean requireAttackKey = true;
    public static boolean cooldownCheck = true;
    public static boolean critTiming = false;
    public static int missChance = 4;
    public static int tickSkipChance = 6;

    public static double velocityHorizontal = 0.55;
    public static double velocityVertical = 1.0;
    public static String velocityMode = "soft";
    public static boolean velocityOnlyWhenHurt = true;

    public static boolean pingScaleDelays = true;
    public static boolean hideHudOnScreenshot = true;
    public static boolean hideHudInDebug = true;

    public static int potSlotMin = 0;
    public static int potSlotMax = 2;

    public static int arrayListColor = 0x3DDCFF;
    public static boolean arrayListRainbow = false;

    /** Premium UX: soft click on module toggle. */
    public static boolean toggleSounds = true;
    /** Show active module count on watermark. */
    public static boolean showActiveCount = true;

    public static final Set<String> favorites = new HashSet<>();

    public static String mode = "dual";
    public static String lastProfile = "sword";

    public static final String[] PROFILE_CYCLE = {
            "scout", "builder", "explore", "anarchy", "sword", "nethpot", "legit"
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
        if (isFavorite(name)) removeFavorite(name);
        else addFavorite(name);
    }

    public static void applyVelocityMode(String m) {
        velocityMode = m == null ? "soft" : m.toLowerCase();
        switch (velocityMode) {
            case "strong" -> velocityHorizontal = 0.42;
            case "medium" -> velocityHorizontal = 0.50;
            default -> {
                velocityMode = "soft";
                velocityHorizontal = 0.55;
            }
        }
        velocityVertical = 1.0;
    }

    public static void applyUtilityConfig() {
        mode = "utility";
        lastProfile = "scout";
        aimMode = "classic";
        targetPriority = "crosshair";
        hideHudOnScreenshot = true;
        hideHudInDebug = true;
        arrayListColor = 0x3DDCFF;
    }

    public static void applyDualConfig() {
        mode = "dual";
        lastProfile = "sword";
        applySwordConfig();
        mode = "dual";
    }

    public static void applySwordConfig() {
        mode = "sword";
        lastProfile = "sword";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 4.25;
        aimFov = 68f;
        aimSmooth = 0.22f;
        aimDeadzone = 1.8f;
        aimMaxStep = 4.5f;
        auraRange = 3.2;
        auraFov = 70f;
        auraMultiTarget = false;
        hitboxExpand = 0.10;
        reachDistance = 3.12;
        reachMode = "soft";
        combatDelayMin = 540;
        combatDelayMax = 700;
        requireAttackKey = true;
        cooldownCheck = true;
        critTiming = false;
        missChance = 4;
        tickSkipChance = 6;
        applyVelocityMode("soft");
        velocityOnlyWhenHurt = true;
        pingScaleDelays = true;
        potSlotMin = 0;
        potSlotMax = 2;
    }

    public static void applySwordAggressiveConfig() {
        applySwordConfig();
        mode = "sword-aggro";
        lastProfile = "swordaggro";
        aimSmooth = 0.28f;
        aimFov = 75f;
        aimRange = 4.5;
        hitboxExpand = 0.12;
        reachDistance = 3.2;
        reachMode = "medium";
        applyVelocityMode("medium");
    }

    public static void applyNethpotConfig() {
        mode = "nethpot";
        lastProfile = "nethpot";
        aimMode = "classic";
        targetPriority = "lowest_hp";
        aimRange = 4.2;
        aimFov = 72f;
        aimSmooth = 0.20f;
        auraRange = 3.2;
        hitboxExpand = 0.11;
        reachDistance = 3.15;
        reachMode = "soft";
        combatDelayMin = 480;
        combatDelayMax = 700;
        requireAttackKey = false;
        cooldownCheck = true;
        missChance = 4;
        tickSkipChance = 6;
        applyVelocityMode("soft");
        potSlotMin = 0;
        potSlotMax = 3;
    }

    public static void applyLegitConfig() {
        mode = "legit";
        lastProfile = "legit";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 4.0;
        aimFov = 55f;
        aimSmooth = 0.16f;
        auraRange = 3.1;
        hitboxExpand = 0.06;
        reachDistance = 3.05;
        reachMode = "soft";
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
        lastProfile = "rage";
        aimMode = "silent";
        targetPriority = "closest";
        aimRange = 5.5;
        aimFov = 120f;
        aimSmooth = 0.40f;
        auraRange = 4.0;
        hitboxExpand = 0.28;
        reachDistance = 3.4;
        reachMode = "strong";
        combatDelayMin = 400;
        combatDelayMax = 560;
        requireAttackKey = false;
        cooldownCheck = false;
        missChance = 1;
        tickSkipChance = 2;
        applyVelocityMode("medium");
    }

    public static void applyKitConfig() {
        applySwordConfig();
        mode = "kit";
        lastProfile = "kit";
        missChance = 5;
        potSlotMin = 1;
        potSlotMax = 3;
    }

    public static String summarize() {
        return String.format("mode=%s profile=%s aim=%s sounds=%s",
                mode, lastProfile, aimMode, toggleSounds);
    }
}
