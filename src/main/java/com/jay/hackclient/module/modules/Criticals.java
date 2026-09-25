package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.util.CombatRequirements;
import com.jay.hackclient.util.RealPackets;

/** Criticals — Jump / Packet / Mini. Packet uses real PlayerMoveC2SPacket offsets. */
public class Criticals extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Jump", "Jump", "Packet", "Mini");

    public Criticals() {
        super("Criticals", "Force critical hit windows", Category.COMBAT);
        addSetting(mode);
    }

    @Override
    public void onTick() {
        if ("Jump".equals(mode.get())) {
            try { com.jay.hackclient.settings.ClientSettings.critTiming = true; } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onDisable() {
        if (!isCritAssistOn()) {
            try { com.jay.hackclient.settings.ClientSettings.critTiming = false; } catch (Throwable ignored) {}
        }
    }

    public void doCritPackets() {
        if (!isEnabled() || mc.player == null) return;
        if (!mc.player.isOnGround()) return;
        if (mc.player.isTouchingWater() || mc.player.isClimbing()) return;
        String m = mode.get();
        if ("Jump".equals(m)) return;
        double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
        if ("Packet".equals(m)) {
            RealPackets.sendPosition(x, y + 0.0625, z, false);
            RealPackets.sendPosition(x, y, z, false);
        } else if ("Mini".equals(m)) {
            RealPackets.sendPosition(x, y + 0.0001, z, false);
            RealPackets.sendPosition(x, y, z, false);
        }
    }

    private static boolean isCritAssistOn() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("CritAssist");
            return m != null && m.isEnabled();
        } catch (Throwable t) { return false; }
    }

    public static boolean isActive() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Criticals");
            return m != null && m.isEnabled();
        } catch (Throwable t) { return false; }
    }

    public static boolean shouldWaitForCrit() {
        return isActive() && CombatRequirements.allowsCriticalHit();
    }

    public static Criticals get() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Criticals");
            return m instanceof Criticals c ? c : null;
        } catch (Throwable t) { return null; }
    }
}
