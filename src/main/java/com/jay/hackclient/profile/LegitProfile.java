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
        Hitboxes.setExpand(0.08);
        enable("AutoSprint");
        enable("AimAssist");
        enable("HUD");
        enable("AntiBot");
    }

    public static void applySemi() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.14);
        enable("AutoSprint");
        enable("AutoSword");
        enable("TriggerBot");
        enable("AimAssist");
        enable("Velocity");
        enable("Hitboxes");
        enable("WTap");
        enable("AutoTotem");
        enable("HUD");
        enable("TargetHUD");
        enable("AntiBot");
    }

    public static void applyRage() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.3);
        enable("KillAura");
        enable("AutoSword");
        enable("AutoSprint");
        enable("Velocity");
        enable("WTap");
        enable("Hitboxes");
        enable("ESP");
        enable("Nametags");
        enable("FullBright");
        enable("AutoTotem");
        enable("HUD");
        enable("TargetHUD");
        enable("AntiBot");
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
        enable("AntiBot");
    }

    public static void applyNethpot() {
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
        enable("AutoPot");
        enable("AutoTotem");
        enable("HUD");
        enable("TargetHUD");
        enable("AntiBot");
    }

    public static void applyUhc() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.12);
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
        enable("AntiBot");
    }

    public static void applyKit() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.14);
        enable("AimAssist");
        enable("TriggerBot");
        enable("AutoSword");
        enable("ShieldBreak");
        enable("AutoSprint");
        enable("Velocity");
        enable("WTap");
        enable("Hitboxes");
        enable("AutoTotem");
        enable("OffhandGap");
        enable("Refill");
        enable("HUD");
        enable("TargetHUD");
        enable("AntiBot");
    }

    public static void applyCrystal() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
        Hitboxes.setExpand(0.1);
        enable("AimAssist");
        enable("AutoTotem");
        enable("OffhandGap");
        enable("Refill");
        enable("AnchorMacro");
        enable("AutoSprint");
        enable("Velocity");
        enable("HUD");
        enable("TargetHUD");
        enable("AntiBot");
    }

    private static void enable(String name) {
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
