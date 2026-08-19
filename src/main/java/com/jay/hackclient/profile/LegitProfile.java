package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;

public final class LegitProfile {

    private LegitProfile() {}

    public static void applyLegit() {
        disableAll();
        Hitboxes.setExpand(0.08);
        on("AutoSprint"); on("AimAssist"); on("HUD"); on("AntiBot");
    }

    public static void applySemi() {
        disableAll();
        Hitboxes.setExpand(0.14);
        on("AutoSprint"); on("AutoSword"); on("TriggerBot"); on("AimAssist");
        on("Velocity"); on("Hitboxes"); on("WTap"); on("JumpReset");
        on("ShieldBreak"); on("AutoTotem"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyRage() {
        unfreeze();
        Hitboxes.setExpand(0.3);
        on("KillAura"); on("AutoSword"); on("AutoSprint"); on("Velocity");
        on("WTap"); on("JumpReset"); on("Hitboxes"); on("ShieldBreak");
        on("ESP"); on("Nametags"); on("FullBright"); on("AutoTotem");
        on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyScout() {
        disableAll();
        on("BaseFinder"); on("SpawnerFinder"); on("PlayerRadar"); on("PortalFinder");
        on("ESP"); on("Nametags"); on("FullBright"); on("HUD"); on("AntiBot");
    }

    public static void applyNethpot() {
        disableAll();
        Hitboxes.setExpand(0.14);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("AutoSprint");
        on("Velocity"); on("Hitboxes"); on("AutoPot"); on("AutoTotem");
        on("JumpReset"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyUhc() {
        disableAll();
        Hitboxes.setExpand(0.12);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("AutoSprint");
        on("Velocity"); on("Hitboxes"); on("AutoGap"); on("AutoHead");
        on("PearlCatch"); on("JumpReset"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyKit() {
        disableAll();
        Hitboxes.setExpand(0.14);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("ShieldBreak");
        on("AutoSprint"); on("Velocity"); on("WTap"); on("JumpReset");
        on("Hitboxes"); on("AutoTotem"); on("OffhandGap"); on("Refill");
        on("AutoBlock"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyCrystal() {
        disableAll();
        Hitboxes.setExpand(0.1);
        on("AimAssist"); on("AutoTotem"); on("OffhandGap"); on("Refill");
        on("AnchorMacro"); on("AutoSprint"); on("Velocity"); on("HUD");
        on("TargetHUD"); on("AntiBot");
    }

    private static void disableAll() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
    }

    private static void unfreeze() {
        if (JayHackClient.moduleManager != null) JayHackClient.moduleManager.unfreeze();
    }

    private static void on(String name) {
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
