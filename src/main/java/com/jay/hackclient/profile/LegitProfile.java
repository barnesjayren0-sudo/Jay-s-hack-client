package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.settings.ClientSettings;

public final class LegitProfile {

    private LegitProfile() {}

    public static void applyLegit() {
        off();
        ClientSettings.applyLegitConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AutoSprint"); on("AimAssist"); on("HUD"); on("AntiBot");
    }

    public static void applySemi() {
        off();
        ClientSettings.applySwordConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AutoSprint"); on("AutoSword"); on("TriggerBot"); on("AimAssist");
        on("Velocity"); on("WTap"); on("JumpReset"); on("ShieldBreak");
        on("NoJumpDelay"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applySword() {
        off();
        ClientSettings.applySwordConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("ShieldBreak");
        on("AutoSprint"); on("Velocity"); on("WTap"); on("JumpReset");
        on("NoJumpDelay"); on("Hitboxes"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applySwordAggressive() {
        off();
        ClientSettings.applySwordAggressiveConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("ShieldBreak");
        on("AutoSprint"); on("Velocity"); on("WTap"); on("JumpReset");
        on("NoJumpDelay"); on("Hitboxes"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyRage() {
        if (JayHackClient.moduleManager != null) JayHackClient.moduleManager.unfreeze();
        ClientSettings.applyRageConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("KillAura"); on("AutoSword"); on("AutoSprint"); on("Velocity");
        on("WTap"); on("JumpReset"); on("NoJumpDelay"); on("Hitboxes"); on("ShieldBreak");
        on("ESP"); on("Nametags"); on("FullBright"); on("AutoTotem");
        on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyScout() {
        off();
        on("BaseFinder"); on("SpawnerFinder"); on("PlayerRadar"); on("PortalFinder");
        on("ESP"); on("Nametags"); on("FullBright"); on("HUD"); on("AntiBot");
    }

    public static void applyNethpot() {
        off();
        ClientSettings.applyNethpotConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("AutoSprint");
        on("Velocity"); on("Hitboxes"); on("AutoPot"); on("PotRefill"); on("AutoTotem");
        on("JumpReset"); on("NoJumpDelay"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyUhc() {
        off();
        ClientSettings.applySwordConfig();
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("AutoSprint");
        on("Velocity"); on("AutoGap"); on("AutoHead"); on("PearlCatch");
        on("JumpReset"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyKit() {
        off();
        ClientSettings.applySwordConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("ShieldBreak");
        on("AutoSprint"); on("Velocity"); on("WTap"); on("JumpReset");
        on("NoJumpDelay"); on("AutoTotem"); on("OffhandGap"); on("Refill"); on("AutoBlock");
        on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyCrystal() {
        off();
        ClientSettings.applyLegitConfig();
        on("AimAssist"); on("AutoTotem"); on("OffhandGap"); on("Refill");
        on("AnchorMacro"); on("AutoSprint"); on("Velocity"); on("HUD");
        on("TargetHUD"); on("AntiBot");
    }

    private static void off() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
    }

    private static void on(String name) {
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
