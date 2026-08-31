package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Shared combat target for HUD — HP, range, armor in one panel. */
public class TargetHUD extends Module {

    public static PlayerEntity currentTarget = null;
    public static float currentDistance = 0;
    public static float currentHp = 0;
    public static float currentMaxHp = 20;
    public static int armorPoints = 0;

    public TargetHUD() {
        super("TargetHUD", "Target HP / range / armor panel", Category.RENDER);
    }

    @Override
    public void onTick() {
        currentTarget = null;
        currentDistance = 0;
        currentHp = 0;
        currentMaxHp = 20;
        armorPoints = 0;
        if (mc.player == null || mc.world == null) return;

        PlayerEntity pick = null;
        try {
            pick = TargetUtil.findCombatTarget(
                    Math.max(ClientSettings.aimRange, ClientSettings.auraRange),
                    Math.max(ClientSettings.aimFov, ClientSettings.auraFov));
        } catch (Throwable ignored) {}

        if (pick == null && mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            if (mc.crosshairTarget instanceof EntityHitResult ehr
                    && ehr.getEntity() instanceof PlayerEntity p
                    && p != mc.player) {
                pick = p;
            }
        }

        if (pick == null) {
            double best = 6;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player || !p.isAlive()) continue;
                if (AntiBot.isBot(p)) continue;
                if (JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;
                double d = mc.player.distanceTo(p);
                if (d < best) {
                    best = d;
                    pick = p;
                }
            }
        }

        if (pick != null) setTarget(pick);
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        currentDistance = 0;
        currentHp = 0;
        armorPoints = 0;
    }

    private void setTarget(PlayerEntity target) {
        if (!target.isAlive() || target.isSpectator() || AntiBot.isBot(target)) return;
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(target.getName().getString())) return;

        currentTarget = target;
        currentDistance = mc.player.distanceTo(target);
        currentHp = target.getHealth() + target.getAbsorptionAmount();
        currentMaxHp = Math.max(1f, target.getMaxHealth());
        try {
            armorPoints = target.getArmor();
        } catch (Throwable t) {
            armorPoints = 0;
        }
    }
}
