package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;

public final class LegitProfile {

    private LegitProfile() {}

    public static void applyLegit() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        enable("AutoSprint");
        enable("AimAssist");
        enable("HUD");
    }

    public static void applySemi() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        enable("AutoSprint");
        enable("AutoSword");
        enable("TriggerBot");
        enable("AimAssist");
        enable("Velocity");
        enable("HUD");
        enable("ESP");
        enable("TargetHUD");
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
        enable("Nametags");
        enable("FullBright");
        enable("HUD");
        enable("TargetHUD");
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
        enable("Nametags");
        enable("FullBright");
        enable("HUD");
    }

    private static void enable(String name) {
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
