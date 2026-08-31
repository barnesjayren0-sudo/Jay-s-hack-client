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

    public static boolean toggleSounds = true;
    public static boolean showActiveCount = true;

    public static final Set<String> favorites = new HashSet<>();

    public static boolean firstLaunchDone = false;

    /** ClickGUI scale 0.85–1.25 */
    public static float guiScale = 1.0f;

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
            case "medium" -> velocityHorizontal = 0.50;
            case "strong" -> velocityHorizontal = 0.42;
            default -> velocityHorizontal = 0.55;
        }
    }

    public static void applyDualConfig() {
        mode = "dual";
        lastProfile = "sword";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 4.25;
        aimFov = 70f;
        aimSmooth = 0.22f;
        auraRange = 3.2;
        hitboxExpand = 0.10;
        reachDistance = 3.12;
        applyVelocityMode("soft");
    }

    public static void applySwordConfig() {
        mode = "sword";
        lastProfile = "sword";
        aimMode = "classic";
        targetPriority = "crosshair";
        aimRange = 4.2;
        aimFov = 65f;
        aimSmooth = 0.20f;
        auraRange = 3.15;
        hitboxExpand = 0.08;
        reachDistance = 3.1;
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
        applyVelocityMode("medium");
    }

    public static void applyKitConfig() {
        applySwordConfig();
        mode = "kit";
        lastProfile = "kit";
        potSlotMin = 1;
        potSlotMax = 3;
    }

    public static String summarize() {
        return String.format("mode=%s profile=%s aim=%s scale=%.2f",
                mode, lastProfile, aimMode, guiScale);
    }
}
