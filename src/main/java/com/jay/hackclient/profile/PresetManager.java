package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Notifications;

/**
 * Built-in presets: Legit / PvP / Survival / Performance.
 * One-click module configuration; custom profiles use ProfileManager.
 */
public final class PresetManager {

    public enum Preset {
        LEGIT("Legit"),
        PVP("PvP"),
        SURVIVAL("Survival"),
        PERFORMANCE("Performance");

        public final String display;
        Preset(String d) { this.display = d; }
    }

    private PresetManager() {}

    public static void apply(Preset preset) {
        if (JayHackClient.moduleManager == null || preset == null) return;
        // Soft reset combat/movement first
        for (Module m : JayHackClient.moduleManager.getModules()) {
            Module.Category c = m.getCategory();
            if (c == Module.Category.COMBAT || c == Module.Category.MOVEMENT
                    || c == Module.Category.PLAYER || c == Module.Category.ANARCHY) {
                if (m.isEnabled()) m.setEnabled(false);
            }
        }

        switch (preset) {
            case LEGIT -> {
                LegitProfile.applyLegit();
                enable("AimAssist", "TriggerBot", "AutoSprint", "SafeWalk", "HUD", "AntiBot");
            }
            case PVP -> {
                LegitProfile.applySword();
                enable("KillAura", "AimAssist", "Velocity", "AutoSword", "ShieldBreak",
                        "WTap", "AutoTotem", "AutoSprint", "HUD", "TargetHUD", "AntiBot");
            }
            case SURVIVAL -> {
                LegitProfile.applyExplore();
                enable("AutoSprint", "AutoTool", "FullBright", "ESP", "StorageESP",
                        "BaseFinder", "HUD", "SafeWalk", "AutoArmor");
            }
            case PERFORMANCE -> {
                // Minimal modules for weak devices (Redmi / Mojo)
                for (Module m : JayHackClient.moduleManager.getModules()) {
                    if (m.isEnabled() && !m.getName().equals("HUD")) m.setEnabled(false);
                }
                enable("HUD", "AutoSprint");
            }
        }

        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        Notifications.push("Preset", preset.display + " applied");
    }

    private static void enable(String... names) {
        for (String n : names) {
            Module m = JayHackClient.moduleManager.getModuleByName(n);
            if (m != null && !m.isEnabled()) m.setEnabled(true);
        }
    }

    public static void applyByName(String name) {
        if (name == null) return;
        for (Preset p : Preset.values()) {
            if (p.name().equalsIgnoreCase(name) || p.display.equalsIgnoreCase(name)) {
                apply(p);
                return;
            }
        }
    }
}
