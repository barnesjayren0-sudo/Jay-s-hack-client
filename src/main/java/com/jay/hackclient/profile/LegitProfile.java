package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;

public final class LegitProfile {

    private LegitProfile() {}

    public static void applyLegit() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.10);
        enable("AutoSprint");
        enable("AimAssist");
        enable("HUD");
    }

    public static void applySemi() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.15);
        enable("AutoSprint");
        enable("AutoSword");
        enable("TriggerBot");
        enable("AimAssist");
        enable("Velocity");
        enable("Hitboxes");
        enable("HUD");
        enable("TargetHUD");
    }

    public static void applyRage() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.35);
        enable("KillAura");
        enable("AutoSword");
        enable("AutoSprint");
        enable("Velocity");
        enable("WTap");
        enable("Hitboxes");
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

    /** Nethpot-oriented stack */
    public static void applyNethpot() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.16);
        enable("AimAssist");
        enable("TriggerBot");
        enable("AutoSword");
        enable("AutoSprint");
        enable("Velocity");
        enable("Hitboxes");
        enable("AutoPot");
        enable("HUD");
        enable("TargetHUD");
    }

    /** UHC-oriented stack */
    public static void applyUhc() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.14);
        enable("AimAssist");
        enable("TriggerBot");
        enable("AutoSword");
        enable("AutoSprint");
        enable("Velocity");
        enable("Hitboxes");
        enable("AutoGap");
        enable("AutoHead");
        enable("PearlCatch");
        enable("HUD");
        enable("TargetHUD");
    }

    private static void enable(String name) {
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
