package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/** Remember logout positions + ESP + optional waypoint save. */
public class LogoutSpots extends Module {

    public static final Map<String, Spot> SPOTS = new ConcurrentHashMap<>();
    public static final List<BaseFinder.Hit> espHits = new CopyOnWriteArrayList<>();

    public final BoolSetting chat = new BoolSetting("Chat", "Announce logouts", true);
    public final BoolSetting boxes = new BoolSetting("Boxes", "Show ESP markers", true);
    public final BoolSetting autoWp = new BoolSetting("AutoWaypoint", "Save logout as waypoint", true);
    public final NumberSetting maxSpots = new NumberSetting("MaxSpots", "Cap stored", 40, 10, 80, 5);
    public final NumberSetting colorR = new NumberSetting("ColorR", "Red", 255, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Green", 80, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Blue", 80, 0, 255, 1);

    private final Map<UUID, Tracked> tracked = new ConcurrentHashMap<>();

    public LogoutSpots() {
        super("LogoutSpots", "Logout positions + ESP", Category.WORLD);
        addSetting(chat);
        addSetting(boxes);
        addSetting(autoWp);
        addSetting(maxSpots);
        addSetting(colorR);
        addSetting(colorG);
        addSetting(colorB);
    }

    public int colorArgb() {
        return 0xFF000000
                | ((colorR.getInt() & 255) << 16)
                | ((colorG.getInt() & 255) << 8)
                | (colorB.getInt() & 255);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getNetworkHandler() == null) return;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            tracked.put(p.getUuid(), new Tracked(p.getName().getString(), p.getBlockPos()));
        }

        for (Map.Entry<UUID, Tracked> e : tracked.entrySet()) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(e.getKey());
            if (entry == null) {
                Tracked t = e.getValue();
                SPOTS.put(t.name, new Spot(t.name, t.pos, System.currentTimeMillis()));
                if (chat.get() && mc.player != null) {
                    mc.player.sendMessage(Text.literal(
                            "§8[§bJay§8] §eLogout §f" + t.name + " §7@ "
                                    + t.pos.getX() + " " + t.pos.getY() + " " + t.pos.getZ()), false);
                }
                if (autoWp.get()) {
                    Waypoints.save("lo_" + t.name, t.pos);
                }
                tracked.remove(e.getKey());
            }
        }

        while (SPOTS.size() > maxSpots.getInt()) {
            String oldest = null;
            long min = Long.MAX_VALUE;
            for (var e : SPOTS.entrySet()) {
                if (e.getValue().time < min) {
                    min = e.getValue().time;
                    oldest = e.getKey();
                }
            }
            if (oldest != null) SPOTS.remove(oldest);
            else break;
        }

        rebuildEsp();
    }

    private void rebuildEsp() {
        espHits.clear();
        if (!boxes.get() || mc.player == null) return;
        int col = colorArgb();
        BlockPos origin = mc.player.getBlockPos();
        for (Spot s : SPOTS.values()) {
            double dist = Math.sqrt(origin.getSquaredDistance(s.pos));
            espHits.add(new BaseFinder.Hit(s.pos, "LO:" + s.name, 15, dist, col));
        }
    }

    @Override
    public void onDisable() {
        tracked.clear();
        espHits.clear();
    }

    public record Spot(String name, BlockPos pos, long time) {}
    private record Tracked(String name, BlockPos pos) {}
}
