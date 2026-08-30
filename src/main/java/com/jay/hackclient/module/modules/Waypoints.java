package com.jay.hackclient.module.modules;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;

/** Save named positions and path with Baritone. */
public class Waypoints extends Module {

    public static final Map<String, BlockPos> POINTS = new LinkedHashMap<>();

    public final BoolSetting announce = new BoolSetting("Announce", "Chat on save", true);

    public Waypoints() {
        super("Waypoints", "Save / goto positions", Category.WORLD);
        addSetting(announce);
    }

    public static void save(String name, BlockPos pos) {
        if (name == null || name.isBlank() || pos == null) return;
        POINTS.put(name.toLowerCase(), pos.toImmutable());
        msg("§aWaypoint §f" + name + " §7@ " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
    }

    public static void saveHere(String name) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;
        save(name, mc.player.getBlockPos());
    }

    public static void gotoWp(String name) {
        BlockPos p = POINTS.get(name.toLowerCase());
        if (p == null) {
            msg("§cUnknown waypoint");
            return;
        }
        boolean ok = BaritoneCompat.pathTo(p);
        msg(ok ? "§aPath → " + name : "§cPath failed (Baritone?)");
    }

    public static void list() {
        if (POINTS.isEmpty()) {
            msg("§7No waypoints");
            return;
        }
        for (var e : POINTS.entrySet()) {
            BlockPos p = e.getValue();
            msg("§f" + e.getKey() + " §7" + p.getX() + " " + p.getY() + " " + p.getZ());
        }
    }

    public static void remove(String name) {
        if (POINTS.remove(name.toLowerCase()) != null) msg("§cRemoved " + name);
        else msg("§cNot found");
    }

    private static void msg(String s) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }

    @Override
    public void onTick() {
        // Command-driven; module only marks feature enabled for HUD
    }
}
