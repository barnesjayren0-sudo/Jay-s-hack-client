package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Remember where players were when they left render / tab. */
public class LogoutSpots extends Module {

    public static final Map<String, Spot> SPOTS = new ConcurrentHashMap<>();

    private final Map<UUID, Tracked> tracked = new ConcurrentHashMap<>();

    public LogoutSpots() {
        super("LogoutSpots", "Mark player leave positions", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        // Update tracked living players in world
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            tracked.put(p.getUuid(), new Tracked(p.getName().getString(), p.getBlockPos()));
        }

        // Detect tab removals
        for (Map.Entry<UUID, Tracked> e : tracked.entrySet()) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(e.getKey());
            if (entry == null) {
                Tracked t = e.getValue();
                SPOTS.put(t.name, new Spot(t.name, t.pos, System.currentTimeMillis()));
                if (mc.player != null) {
                    mc.player.sendMessage(Text.literal(
                            "§8[§bJay§8] §eLogout §f" + t.name + " §7@ "
                                    + t.pos.getX() + " " + t.pos.getY() + " " + t.pos.getZ()), false);
                }
                tracked.remove(e.getKey());
            }
        }

        // Cap map size
        if (SPOTS.size() > 40) {
            String oldest = null;
            long min = Long.MAX_VALUE;
            for (var e : SPOTS.entrySet()) {
                if (e.getValue().time < min) {
                    min = e.getValue().time;
                    oldest = e.getKey();
                }
            }
            if (oldest != null) SPOTS.remove(oldest);
        }
    }

    @Override
    public void onDisable() {
        tracked.clear();
    }

    public record Spot(String name, BlockPos pos, long time) {}
    private record Tracked(String name, BlockPos pos) {}
}
