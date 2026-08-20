package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Optional vanilla-crit timing gate. It never changes gravity or launches the
 * player; callers simply wait for the normal falling window.
 */
public class CritAssist extends Module {
    public CritAssist() {
        super("CritAssist", "Waits for a small vanilla falling window", Category.COMBAT);
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
        if (player == null || player.isOnGround() || player.isClimbing()
                || player.hasVehicle() || player.isTouchingWater() || player.isInLava()) {
            return false;
        }
        return player.fallDistance > 0.08f && player.fallDistance < 0.65f;
    }
}