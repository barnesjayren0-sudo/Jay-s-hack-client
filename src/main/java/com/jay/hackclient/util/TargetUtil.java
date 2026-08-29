package com.jay.hackclient.util;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.modules.AntiBot;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Shared target selection for AimAssist / KillAura / TriggerBot / TargetStrafe. */
public final class TargetUtil {

    private static PlayerEntity cached;
    private static long cachedAt;

    private TargetUtil() {}

    /** Fast path used by combat modules — 50ms cache reduces flicker. */
    public static PlayerEntity findCombatTarget(double range, float fov) {
        long now = System.currentTimeMillis();
        if (cached != null && now - cachedAt < 50) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && cached.isAlive()
                    && mc.player.distanceTo(cached) <= range + 0.5) {
                return cached;
            }
        }
        PlayerEntity t = find(range, fov);
        cached = t;
        cachedAt = now;
        return t;
    }

    public static void invalidate() {
        cached = null;
        cachedAt = 0;
    }

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

            if (prio.equals("crosshair") && yawDiff > fov) continue;
            if (!prio.equals("crosshair") && fov < 180 && yawDiff > fov * 1.2f) continue;

            double score = switch (prio) {
                case "lowest_hp" -> p.getHealth() + p.getAbsorptionAmount() + dist * 0.01;
                case "closest" -> dist;
                default -> yawDiff + pitchDiff * 0.5;
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
