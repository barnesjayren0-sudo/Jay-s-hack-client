package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;

/**
 * Applies safer default combos. Nothing is undetectable on modern AC.
 * These profiles just reduce blatant flags.
 */
public final class LegitProfile {

    private LegitProfile() {}

    public static void applyLegit() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();

        enable("AutoSprint");
        enable("HUD");
        // Light assist only — user must toggle combat manually
    }

    public static void applySemi() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();

        enable("AutoSprint");
        enable("AutoSword");
        enable("TriggerBot");
        enable("Velocity");
        enable("HUD");
        enable("ESP");
    }

    public static void applyRage() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.unfreeze();

        enable("KillAura");
        enable("AutoSword");
        enable("AutoSprint");
        enable("Velocity");
        enable("WTap");
        enable("Speed");
        enable("ESP");
        enable("FullBright");
        enable("HUD");
    }

    public static void applyScout() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();

        enable("BaseFinder");
        enable("SpawnerFinder");
        enable("PlayerRadar");
        enable("PortalFinder");
        enable("ESP");
        enable("FullBright");
        enable("HUD");
    }

    private static void enable(String name) {
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
