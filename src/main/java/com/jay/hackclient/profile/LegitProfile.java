package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.settings.ClientSettings;

/** Profiles — utility-first. Combat profiles kept optional. */
public final class LegitProfile {

    private LegitProfile() {}

    /** Finders + radar + light visuals */
    public static void applyScout() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "scout";
        ClientSettings.mode = "scout";
        on("BaseFinder");
        on("StorageFinder");
        on("BuildFinder");
        on("BeaconFinder");
        on("SpawnerFinder");
        on("PortalFinder");
        on("PlayerRadar");
        on("ESP");
        on("Nametags");
        on("FullBright");
        on("StorageESP");
        on("HUD");
        on("AntiBot");
    }

    /** Building / bridging QoL */
    public static void applyBuilder() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "builder";
        ClientSettings.mode = "builder";
        on("Scaffold");
        on("AutoTool");
        on("SafeWalk");
        on("AutoSprint");
        on("FullBright");
        on("ESP");
        on("HUD");
        on("AntiBot");
    }

    /** Explore + path */
    public static void applyExplore() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "explore";
        ClientSettings.mode = "explore";
        on("BaseFinder");
        on("PlayerRadar");
        on("PortalFinder");
        on("FullBright");
        on("ESP");
        on("Nametags");
        on("AutoSprint");
        on("NoFall");
        on("HUD");
        on("AntiBot");
    }

    /** Minimal utility HUD stack */
    public static void applyUtility() {
        off();
        ClientSettings.applyUtilityConfig();
        on("FullBright");
        on("AutoSprint");
        on("AutoTool");
        on("HUD");
        on("AntiBot");
    }

    /** Anarchy survival stack — safety + movement + storage */
    public static void applyAnarchy() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "anarchy";
        ClientSettings.mode = "anarchy";
        // Safety
        on("AutoLog");
        on("AntiVoid");
        on("AutoTotem");
        on("NoFall");
        // Movement (Fly off by default — toggle with G when needed)
        on("Step");
        on("AutoSprint");
        // Combat soft
        on("Velocity");
        on("AutoArmor");
        // World intel
        on("BaseFinder");
        on("StorageFinder");
        on("StorageESP");
        on("PlayerRadar");
        on("ESP");
        on("Nametags");
        on("FullBright");
        on("HUD");
        on("AntiBot");
        on("MiddleClickPearl");
    }

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
        on("AutoSprint"); on("WTap"); on("JumpReset");
        on("NoJumpDelay"); on("Hitboxes");
        on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applySwordAggressive() {
        off();
        ClientSettings.applySwordAggressiveConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("ShieldBreak");
        on("AutoSprint"); on("Velocity"); on("WTap"); on("JumpReset");
        on("NoJumpDelay"); on("Hitboxes");
        on("HUD"); on("TargetHUD"); on("AntiBot");
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

    public static void applyNethpot() {
        off();
        ClientSettings.applyNethpotConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("AutoSprint");
        on("Hitboxes"); on("AutoPot"); on("PotRefill"); on("AutoTotem");
        on("PearlAssist"); on("JumpReset"); on("NoJumpDelay");
        on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyUhc() {
        off();
        ClientSettings.applySwordConfig();
        ClientSettings.lastProfile = "uhc";
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("AutoSprint");
        on("AutoGap"); on("AutoHead"); on("PearlCatch"); on("PearlAssist");
        on("JumpReset"); on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyKit() {
        off();
        ClientSettings.applyKitConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist"); on("TriggerBot"); on("AutoSword"); on("ShieldBreak");
        on("AutoSprint"); on("WTap"); on("JumpReset");
        on("NoJumpDelay"); on("AutoTotem"); on("OffhandGap"); on("Refill"); on("AutoBlock");
        on("HUD"); on("TargetHUD"); on("AntiBot");
    }

    public static void applyCrystal() {
        off();
        ClientSettings.applyLegitConfig();
        on("AimAssist"); on("AutoTotem"); on("OffhandGap"); on("Refill");
        on("AnchorMacro"); on("AutoSprint"); on("HUD"); on("TargetHUD"); on("AntiBot");
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
