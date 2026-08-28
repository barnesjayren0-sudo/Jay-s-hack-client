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
 * DofNear — client-side "nearby" detector for utility testing.
 *
 * Scope:
 *   - Only entities the client has already loaded (render / entity tracking range).
 *   - Does NOT locate players outside loaded data (not seed/RNG / Randar-style).
 *
 * Education (client vs server):
 *   - Client distance is computed from local entity positions (interpolation / last tick).
 *   - The server has its own authoritative positions; what you see can lag by ping.
 *   - Anti-cheats often validate movement using server-side deltas per tick, reach
 *     on attack packets, and packet rate. Spamming position packets or claiming
 *     impossible distances is what gets flagged — this module only READS locals.
 */
public class DofNear extends Module {

    /** Max distance (blocks) to consider "near". Tunable for local testing. */
    public static double range = 48.0;

    /** Minimum milliseconds between chat reports (avoids spam). */
    public static int reportDelayMs = 3000;

    /** If true, print a chat line when someone enters range. */
    public static boolean chatAlert = true;

    /** If true, include non-player living entities (mobs). Default: players only. */
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

    /**
     * Core targeting: distance from local player eye/pos to each loaded entity.
     *
     * Client-side math:
     *   distance = localPlayer.pos.distanceTo(other.pos)
     * Server-side reality:
     *   The server may still treat that player as slightly elsewhere until the
     *   next movement packet is processed. Never assume client distance == hit validation.
     */
    private void scanAndReport(boolean force) {
        if (mc.player == null || mc.world == null) return;

        Vec3d origin = mc.player.getPos();
        double r = Math.max(1.0, Math.min(128.0, range));

        // Optional AABB pre-filter — cheaper than full world iterate on weak phones
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

                double dist = origin.distanceTo(player.getPos());
                if (dist > r) continue;

                hits.add(new Hit(name, dist, player.getBlockPos().getX(),
                        player.getBlockPos().getY(), player.getBlockPos().getZ(), true));
            } else if (includeMobs) {
                double dist = origin.distanceTo(entity.getPos());
                if (dist > r) continue;
                String label = entity.getName().getString();
                hits.add(new Hit(label, dist, entity.getBlockPos().getX(),
                        entity.getBlockPos().getY(), entity.getBlockPos().getZ(), false));
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

        // Alert only on new names if not forced full dump
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

    /** Manual scan from command / GUI if you wire one. */
    public void forceScan() {
        scanAndReport(true);
    }

    /*
     * -------------------------------------------------------------------------
     * PACKET NOTES (educational template — READ ONLY in this module)
     * -------------------------------------------------------------------------
     * Modern Yarn / Fabric does not use legacy names like "CPacketPlayer".
     * Client → server movement is typically carried by packets such as:
     *   - PlayerMoveC2SPacket (and subclasses: Full, PositionAndOnGround, ...)
     *   - PlayerActionC2SPacket / HandSwingC2SPacket for actions
     *
     * What anti-cheats commonly inspect:
     *   1) Packet rate — too many moves per second vs vanilla cap
     *   2) Delta per tick — distance traveled vs max speed (sprint/fly/vehicle)
     *  3) Reach — attack packet target distance vs server positions
     *   4) Order / timing — impossible sequences (e.g. dig + attack + blink)
     *
     * Safe local testing pattern:
     *   - Observe packets with a logger mixin (debug builds only)
     *   - Do NOT inject forged positions to "teleport" or extend reach on live servers
     *
     * Example skeleton (NOT enabled here — documentation only):
     *
     *   // @Mixin(ClientConnection.class)
     *   // on send(Packet<?>):
     *   //   if (packet instanceof PlayerMoveC2SPacket move) { log(move); }
     *
     * Sending custom payloads requires a registered CustomPayload id on both
     * client and server; random Serverbound payloads will be ignored or kick.
     * -------------------------------------------------------------------------
     */

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
