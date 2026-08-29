package com.jay.hackclient.util;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.modules.AntiBot;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Shared target selection — priority modes inspired by modern clients. */
public final class TargetUtil {

    private TargetUtil() {}

    public static PlayerEntity find(double range, float fov) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return null;

        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;
        Vec3d eye = mc.player.getEyePos();
        String prio = ClientSettings.targetPriority == null ? "crosshair" : ClientSettings.targetPriority;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try {
                if (AntiBot.isBot(p)) continue;
            } catch (Throwable ignored) {}
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;

            double dist = mc.player.distanceTo(p);
            if (dist > range) continue;

            float[] rot = rotationsTo(eye, p.getBoundingBox().getCenter());
            float yawDiff = Math.abs(MathHelper.wrapDegrees(rot[0] - mc.player.getYaw()));
            float pitchDiff = Math.abs(MathHelper.wrapDegrees(rot[1] - mc.player.getPitch()));
            if (yawDiff > fov * 0.5f && pitchDiff > fov * 0.5f && fov < 180) {
                // soft FOV gate for crosshair priority
                if (prio.equals("crosshair") && yawDiff > fov) continue;
            }

            double score = switch (prio) {
                case "lowest_hp" -> p.getHealth() + p.getAbsorptionAmount();
                case "closest" -> dist;
                default -> yawDiff + pitchDiff * 0.5; // crosshair
            };

            if (score < bestScore) {
                bestScore = score;
                best = p;
            }
        }
        return best;
    }

    public static float[] rotationsTo(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, dist)));
        return new float[]{yaw, pitch};
    }
}
