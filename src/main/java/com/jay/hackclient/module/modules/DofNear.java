package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DofNear — client-side nearby detector (loaded entities only).
 */
public class DofNear extends Module {

    public static double range = 48.0;
    public static int reportDelayMs = 3000;
    public static boolean chatAlert = true;
    public static boolean includeMobs = false;

    private long lastReportMs;
    private final List<String> lastNames = new ArrayList<>();

    public DofNear() {
        super(
                "DofNear",
                "Detect entities within configurable range",
                Category.WORLD
        );
    }

    @Override
    public void onEnable() {
        lastNames.clear();
        lastReportMs = 0;
        scanAndReport(true);
    }

    @Override
    public void onDisable() {
        lastNames.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastReportMs < reportDelayMs) return;

        scanAndReport(false);
    }

    /** 1.21.11: use getX/Y/Z — getPos() is not mapped on Entity. */
    private static Vec3d pos(Entity e) {
        return new Vec3d(e.getX(), e.getY(), e.getZ());
    }

    private void scanAndReport(boolean force) {
        if (mc.player == null || mc.world == null) return;

        Vec3d origin = pos(mc.player);
        double r = Math.max(1.0, Math.min(128.0, range));
        Box searchBox = mc.player.getBoundingBox().expand(r);

        List<Hit> hits = new ArrayList<>();

        for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox)) {
            if (!entity.isAlive()) continue;

            if (entity instanceof PlayerEntity player) {
                if (player.isSpectator()) continue;
                if (AntiBot.isBot(player)) continue;
                String name = player.getName().getString();
                if (JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(name)) continue;

                double dist = origin.distanceTo(pos(player));
                if (dist > r) continue;

                hits.add(new Hit(name, dist,
                        player.getBlockPos().getX(),
                        player.getBlockPos().getY(),
                        player.getBlockPos().getZ(),
                        true));
            } else if (includeMobs) {
                double dist = origin.distanceTo(pos(entity));
                if (dist > r) continue;
                String label = entity.getName().getString();
                hits.add(new Hit(label, dist,
                        entity.getBlockPos().getX(),
                        entity.getBlockPos().getY(),
                        entity.getBlockPos().getZ(),
                        false));
            }
        }

        hits.sort(Comparator.comparingDouble(h -> h.dist));

        long now = System.currentTimeMillis();
        if (!force && now - lastReportMs < reportDelayMs) return;
        lastReportMs = now;

        if (hits.isEmpty()) {
            if (force) {
                mc.player.sendMessage(Text.literal(
                        "§8[§bDofNear§8] §7Nobody within §f" + (int) r + "m"), false);
            }
            lastNames.clear();
            return;
        }

        if (!force && chatAlert) {
            for (Hit h : hits) {
                if (!lastNames.contains(h.name)) {
                    mc.player.sendMessage(Text.literal(String.format(
                            "§8[§bDofNear§8] §a+ §f%s §7%.1fm §8@ §b%d %d %d",
                            h.name, h.dist, h.x, h.y, h.z)), false);
                }
            }
        } else if (force) {
            mc.player.sendMessage(Text.literal(
                    "§8[§bDofNear§8] §f" + hits.size() + " within " + (int) r + "m:"), false);
            int shown = 0;
            for (Hit h : hits) {
                if (shown++ >= 8) break;
                mc.player.sendMessage(Text.literal(String.format(
                        "§8[§bDofNear§8] §f%s §7%.1fm §8@ §b%d %d %d",
                        h.name, h.dist, h.x, h.y, h.z)), false);
            }
        }

        lastNames.clear();
        for (Hit h : hits) lastNames.add(h.name);
    }

    public void forceScan() {
        scanAndReport(true);
    }

    private static final class Hit {
        final String name;
        final double dist;
        final int x, y, z;
        final boolean player;

        Hit(String name, double dist, int x, int y, int z, boolean player) {
            this.name = name;
            this.dist = dist;
            this.x = x;
            this.y = y;
            this.z = z;
            this.player = player;
        }
    }
}
