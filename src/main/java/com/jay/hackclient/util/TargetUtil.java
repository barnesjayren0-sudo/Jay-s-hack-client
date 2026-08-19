package com.jay.hackclient.util;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.modules.AntiBot;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;

public final class TargetUtil {

    private TargetUtil() {}

    public static PlayerEntity findCombatTarget(double range, float fov) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return null;

        String prio = ClientSettings.targetPriority;

        if ("crosshair".equals(prio) && mc.crosshairTarget instanceof EntityHitResult ehr) {
            if (ehr.getEntity() instanceof PlayerEntity pe && isValid(pe, mc, range)) {
                return pe;
            }
        }

        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!isValid(p, mc, range)) continue;

            double d = mc.player.distanceTo(p);
            float dyaw = yawDiff(mc, p);
            if (dyaw > fov + 15) continue;

            double score;
            switch (prio) {
                case "lowest_hp" -> score = p.getHealth() + p.getAbsorptionAmount() + d * 0.05;
                case "closest" -> score = d;
                default -> score = d * 0.5 + dyaw * 0.1; // balanced / crosshair-like
            }

            if (score < bestScore) {
                bestScore = score;
                best = p;
            }
        }
        return best;
    }

    private static boolean isValid(PlayerEntity p, MinecraftClient mc, double range) {
        if (p == mc.player || !p.isAlive() || p.isSpectator()) return false;
        if (AntiBot.isBot(p)) return false;
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(p.getName().getString())) return false;
        return mc.player.distanceTo(p) <= range;
    }

    private static float yawDiff(MinecraftClient mc, PlayerEntity p) {
        double dx = p.getX() - mc.player.getX();
        double dz = p.getZ() - mc.player.getZ();
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        return Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
    }
}
