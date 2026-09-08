package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.CombatRequirements;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Vanilla crit timing gate only — no packet no-ground / no forced hops.
 * Window tuned like LB CriticalsJump "wait for fall" idea, without minijumps.
 */
public class CritAssist extends Module {

    public final NumberSetting minFall = new NumberSetting("MinFall", "Min fall distance", 0.08, 0.01, 0.3, 0.01);
    public final NumberSetting maxFall = new NumberSetting("MaxFall", "Max fall distance", 0.55, 0.2, 1.2, 0.05);

    public CritAssist() {
        super("CritAssist", "Wait for vanilla fall window", Category.COMBAT);
        addSetting(minFall);
        addSetting(maxFall);
    }

    @Override
    public void onEnable() {
        com.jay.hackclient.settings.ClientSettings.critTiming = true;
    }

    @Override
    public void onDisable() {
        com.jay.hackclient.settings.ClientSettings.critTiming = false;
    }

    public static boolean canAttackNow(PlayerEntity player) {
        if (player == null) return false;
        if (!CombatRequirements.allowsCriticalHit()) return false;

        float min = 0.08f;
        float max = 0.55f;
        try {
            Module mod = com.jay.hackclient.JayHackClient.moduleManager != null
                    ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("CritAssist") : null;
            if (mod instanceof CritAssist ca) {
                min = ca.minFall.getFloat();
                max = ca.maxFall.getFloat();
            }
        } catch (Throwable ignored) {}

        float fd = player.fallDistance;
        return fd > min && fd < max;
    }
}
