package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BackTrack — hit enemies at their recent server-side positions.
 * Latency / Distance / Smart modes; configurable history window, samples and range.
 * Real attack packets only (PlayerInteractEntityC2SPacket via RealPackets).
 */
public class BackTrack extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Backtrack style", "Smart", "Latency", "Distance", "Smart");
    public final NumberSetting delay = new NumberSetting("Delay", "History window ms", 120, 40, 400, 10);
    public final NumberSetting range = new NumberSetting("Range", "Max hit range", 3.2, 2.8, 6.0, 0.05);
    public final NumberSetting minRange = new NumberSetting("Min Range", "Only BT if current dist > this", 2.6, 1.5, 3.5, 0.05);
    public final NumberSetting samples = new NumberSetting("Samples", "Positions per player", 12, 4, 30, 1);
    public final BoolSetting playersOnly = new BoolSetting("Players Only", "Only track players", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla attack packets", true);
    public final BoolSetting autoHit = new BoolSetting("Auto Hit", "Attack when BT pos valid", false);

    private final Map<UUID, ArrayDeque<Sample>> history = new ConcurrentHashMap<>();
    private long lastAttack;

    public BackTrack() {
        super("BackTrack", "Hit enemies at recent positions", Category.COMBAT);
        addSetting(mode); addSetting(delay); addSetting(range); addSetting(minRange);
        addSetting(samples); addSetting(playersOnly); addSetting(realPackets); addSetting(autoHit);
    }

    @Override
    public void onDisable() {
        history.clear();
        setTag(null);
    }

    @Override
    public void onTick() {
        try { if (!com.jay.hackclient.util.CombatManager.canCombatModulesRun()) return; } catch (Throwable ignored) {}
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        long window = (long) delay.get();
        int maxSamples = samples.getInt();

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            try {
                if (JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;
            } catch (Throwable ignored) {}
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}

            UUID id = p.getUuid();
            ArrayDeque<Sample> q = history.computeIfAbsent(id, k -> new ArrayDeque<>());
            q.addLast(new Sample(now, new Vec3d(p.getX(), p.getY(), p.getZ()), p.getBoundingBox()));
            while (q.size() > maxSamples) q.removeFirst();
            while (!q.isEmpty() && now - q.peekFirst().time > window + 50) q.removeFirst();
        }

        Iterator<Map.Entry<UUID, ArrayDeque<Sample>>> it = history.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, ArrayDeque<Sample>> e = it.next();
            boolean present = false;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p.getUuid().equals(e.getKey())) { present = true; break; }
            }
            if (!present) it.remove();
        }

        if (autoHit.get()) {
            PlayerEntity target = findBacktrackTarget();
            if (target != null && canBacktrackHit(target)
                    && now - lastAttack >= 250
                    && mc.player.getAttackCooldownProgress(0.5f) >= 0.9f) {
                Sample s = bestSample(target);
                if (s != null) {
                    if (realPackets.get()) RealPackets.attackEntity(target);
                    else if (mc.interactionManager != null) {
                        mc.interactionManager.attackEntity(mc.player, target);
                        mc.player.swingHand(Hand.MAIN_HAND);
                    }
                    lastAttack = now;
                    setTag(String.format("%.1fm", distTo(s.pos)));
                }
            }
        } else {
            PlayerEntity t = findBacktrackTarget();
            setTag(t != null && canBacktrackHit(t) ? String.format("BT %.1f", mc.player.distanceTo(t)) : null);
        }
    }

    public boolean canBacktrackHit(Entity entity) {
        if (!isEnabled() || mc.player == null || entity == null) return false;
        if (playersOnly.get() && !(entity instanceof PlayerEntity)) return false;
        if (!(entity instanceof PlayerEntity p)) return false;

        double cur = mc.player.distanceTo(p);
        double maxR = range.get();
        if (cur <= maxR) return true;
        if (cur < minRange.get()) return false;

        Sample s = bestSample(p);
        if (s == null) return false;
        double histDist = distTo(s.pos);

        return switch (mode.get()) {
            case "Latency" -> histDist <= maxR;
            case "Distance" -> histDist <= maxR && cur <= maxR + 1.5;
            default -> {
                double reach = Reach.isActive() ? Reach.getReach() : 3.0;
                yield histDist <= Math.max(maxR, reach + 0.15);
            }
        };
    }

    public Sample bestSample(PlayerEntity p) {
        ArrayDeque<Sample> q = history.get(p.getUuid());
        if (q == null || q.isEmpty()) return null;
        long now = System.currentTimeMillis();
        long window = (long) delay.get();
        Sample best = null;
        double bestD = Double.MAX_VALUE;
        for (Sample s : q) {
            if (now - s.time > window) continue;
            double d = distTo(s.pos);
            if (d < bestD) { bestD = d; best = s; }
        }
        return best;
    }

    public Box getHistoricalBox(PlayerEntity p) {
        Sample s = bestSample(p);
        return s != null ? s.box : null;
    }

    public Vec3d getHistoricalPos(PlayerEntity p) {
        Sample s = bestSample(p);
        return s != null ? s.pos : null;
    }

    private PlayerEntity findBacktrackTarget() {
        PlayerEntity best = null;
        double bestD = range.get() + 2.0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestD && canBacktrackHit(p)) { bestD = d; best = p; }
        }
        return best;
    }

    private double distTo(Vec3d pos) {
        return mc.player.getEyePos().distanceTo(pos.add(0, 0.9, 0));
    }

    public static BackTrack get() {
        try {
            Module m = JayHackClient.moduleManager.getModuleByName("BackTrack");
            return m instanceof BackTrack b ? b : null;
        } catch (Throwable t) { return null; }
    }

    public static boolean allowsHit(Entity entity) {
        BackTrack bt = get();
        if (bt == null || !bt.isEnabled()) return false;
        return bt.canBacktrackHit(entity);
    }

    public static final class Sample {
        public final long time;
        public final Vec3d pos;
        public final Box box;
        public Sample(long time, Vec3d pos, Box box) {
            this.time = time; this.pos = pos; this.box = box;
        }
    }
}
