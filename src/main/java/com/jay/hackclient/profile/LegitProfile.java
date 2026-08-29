package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.module.modules.Scaffold;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.settings.ClientSettings;

/** Built-in profiles — sword & anarchy are the v1.40 defaults. */
public final class LegitProfile {

    private LegitProfile() {}

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
        on("LogoutSpots");
        on("HUD");
        on("InfoHUD");
        on("AntiBot");
    }

    public static void applyBuilder() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "builder";
        ClientSettings.mode = "builder";
        on("Scaffold");
        setScaffoldMode("Telly");
        on("AutoTool");
        on("SafeWalk");
        on("AutoSprint");
        on("FullBright");
        on("ESP");
        on("HUD");
        on("AntiBot");
    }

    public static void applyExplore() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "explore";
        ClientSettings.mode = "explore";
        on("BaseFinder");
        on("PlayerRadar");
        on("PortalFinder");
        on("NewChunks");
        on("FullBright");
        on("ESP");
        on("Nametags");
        on("AutoSprint");
        on("NoFall");
        on("HUD");
        on("AntiBot");
    }

    public static void applyUtility() {
        off();
        ClientSettings.applyUtilityConfig();
        on("FullBright");
        on("AutoSprint");
        on("AutoTool");
        on("HUD");
        on("AntiBot");
    }

    /** Default anarchy stack — finders + survival QoL (no fly). */
    public static void applyAnarchy() {
        off();
        ClientSettings.applyUtilityConfig();
        ClientSettings.lastProfile = "anarchy";
        ClientSettings.mode = "anarchy";
        ClientSettings.applyVelocityMode("soft");

        on("AutoLog");
        on("AntiVoid");
        on("AutoTotem");
        on("NoFall");
        on("Step");
        on("AutoSprint");
        on("Velocity");
        on("AutoArmor");
        on("BaseFinder");
        on("StorageFinder");
        on("StorageESP");
        on("PlayerRadar");
        on("ESP");
        on("Nametags");
        on("FullBright");
        on("HUD");
        on("InfoHUD");
        on("AntiBot");
        on("MiddleClickPearl");
        on("HoleESP");
        on("Jesus");
        on("LogoutSpots");
        // Fly / BoatFly / Burrow / AutoTrap / Surround — manual
    }

    public static void applyLegit() {
        off();
        ClientSettings.applyLegitConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AutoSprint");
        on("AimAssist");
        on("HUD");
        on("AntiBot");
    }

    public static void applySemi() {
        off();
        ClientSettings.applySwordConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AutoSprint");
        on("AutoSword");
        on("TriggerBot");
        on("AimAssist");
        on("ComboHit");
        on("Velocity");
        on("WTap");
        on("JumpReset");
        on("ShieldBreak");
        on("NoJumpDelay");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    /** Default sword PvP stack for v1.40. */
    public static void applySword() {
        off();
        ClientSettings.applySwordConfig();
        ClientSettings.lastProfile = "sword";
        ClientSettings.mode = "sword";
        ClientSettings.applyVelocityMode("soft");
        Hitboxes.setExpand(ClientSettings.hitboxExpand);

        on("AimAssist");
        on("TriggerBot");
        on("ComboHit");
        on("AutoSword");
        on("ShieldBreak");
        on("AutoSprint");
        on("KeepSprint");
        on("Velocity");
        on("WTap");
        on("JumpReset");
        on("NoJumpDelay");
        on("Hitboxes");
        on("AutoGap");
        on("HUD");
        on("TargetHUD");
        on("ReachHUD");
        on("AntiBot");
    }

    public static void applySwordAggressive() {
        off();
        ClientSettings.applySwordAggressiveConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist");
        on("TriggerBot");
        on("ComboHit");
        on("AutoSword");
        on("ShieldBreak");
        on("AutoSprint");
        on("Velocity");
        on("WTap");
        on("JumpReset");
        on("NoJumpDelay");
        on("Hitboxes");
        on("AutoGap");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    public static void applyRage() {
        if (JayHackClient.moduleManager != null) JayHackClient.moduleManager.unfreeze();
        ClientSettings.applyRageConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("KillAura");
        on("ComboHit");
        on("AutoSword");
        on("AutoSprint");
        on("Velocity");
        on("WTap");
        on("JumpReset");
        on("NoJumpDelay");
        on("Hitboxes");
        on("ShieldBreak");
        on("ESP");
        on("Nametags");
        on("FullBright");
        on("AutoTotem");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    public static void applyNethpot() {
        off();
        ClientSettings.applyNethpotConfig();
        Hitboxes.setExpand(ClientSettings.hitboxExpand);
        on("AimAssist");
        on("TriggerBot");
        on("ComboHit");
        on("AutoSword");
        on("AutoSprint");
        on("Hitboxes");
        on("AutoPot");
        on("PotRefill");
        on("AutoTotem");
        on("AutoGap");
        on("PearlAssist");
        on("JumpReset");
        on("NoJumpDelay");
        on("Velocity");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    public static void applyUhc() {
        off();
        ClientSettings.applySwordConfig();
        ClientSettings.lastProfile = "uhc";
        on("AimAssist");
        on("TriggerBot");
        on("ComboHit");
        on("AutoSword");
        on("AutoGap");
        on("AutoSprint");
        on("Velocity");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    public static void applyKit() {
        off();
        ClientSettings.applyKitConfig();
        on("AimAssist");
        on("TriggerBot");
        on("ComboHit");
        on("AutoSword");
        on("AutoSprint");
        on("Velocity");
        on("Scaffold");
        setScaffoldMode("Telly");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    public static void applyCrystal() {
        off();
        ClientSettings.lastProfile = "crystal";
        on("AutoCrystal");
        on("HoleESP");
        on("Surround");
        on("AutoTotem");
        on("AutoSprint");
        on("Velocity");
        on("HUD");
        on("TargetHUD");
        on("AntiBot");
    }

    private static void setScaffoldMode(String mode) {
        if (JayHackClient.moduleManager == null) return;
        Module m = JayHackClient.moduleManager.getModuleByName("Scaffold");
        if (m instanceof Scaffold sc) {
            for (var s : sc.getSettings()) {
                if (s instanceof ModeSetting ms && s.getName().equalsIgnoreCase("Mode")) {
                    ms.set(mode);
                }
            }
        }
    }

    private static void off() {
        if (JayHackClient.moduleManager == null) return;
        JayHackClient.moduleManager.disableAll();
        JayHackClient.moduleManager.unfreeze();
    }

    private static void on(String name) {
        if (JayHackClient.moduleManager == null) return;
        Module m = JayHackClient.moduleManager.getModuleByName(name);
        if (m != null) m.setEnabled(true);
    }
}
