package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;

/** Prefer crit timing — pairs with CritAssist. */
public class Criticals extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Jump", "Jump", "Packet");

    public Criticals() {
        super("Criticals", "Help land critical hits", Category.COMBAT);
        addSetting(mode);
    }

    @Override
    public void onTick() {
        // CritAssist / ComboHit read this module being enabled
        // Packet mode is intentionally soft / no forced packets here for safety
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
}
