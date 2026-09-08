package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.util.CombatRequirements;

/**
 * Crit helper — Jump timing only by default.
 * Packet mode is a no-op stub for safety (ghost client).
 */
public class Criticals extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Jump", "Jump", "Packet");

    public Criticals() {
        super("Criticals", "Prefer critical hit windows", Category.COMBAT);
        addSetting(mode);
    }

    @Override
    public void onTick() {
        // Jump mode: enable CritAssist timing semantics without packet hacks
        if ("Jump".equals(mode.get())) {
            com.jay.hackclient.settings.ClientSettings.critTiming = true;
        }
    }

    @Override
    public void onDisable() {
        if (!isCritAssistOn()) {
            com.jay.hackclient.settings.ClientSettings.critTiming = false;
        }
    }

    private static boolean isCritAssistOn() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("CritAssist");
            return m != null && m.isEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isActive() {
        try {
            if (com.jay.hackclient.JayHackClient.moduleManager == null) return false;
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Criticals");
            return m != null && m.isEnabled();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean shouldWaitForCrit() {
        return isActive() && CombatRequirements.allowsCriticalHit();
    }
}
