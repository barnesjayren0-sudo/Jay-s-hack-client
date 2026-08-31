package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedHashMap;
import java.util.Map;

/** Save named positions and path with Baritone. Persisted via ConfigManager. */
public class Waypoints extends Module {

    public static final Map<String, BlockPos> POINTS = new LinkedHashMap<>();

    public final BoolSetting announce = new BoolSetting("Announce", "Chat on save", true);

    public Waypoints() {
        super("Waypoints", "Save / goto positions", Category.WORLD);
        addSetting(announce);
    }

    public static boolean tryCommand(String[] args) {
        if (args == null || args.length < 2) return false;
        String cmd = args[1].toLowerCase();
        if (!cmd.equals("wp") && !cmd.equals("waypoint")) return false;

        if (args.length < 3) {
            msg("§f.jay wp save <name> | goto <name> | list | del <name>");
            return true;
        }
        String sub = args[2].toLowerCase();
        if (sub.equals("list")) { list(); return true; }
        if (sub.equals("save") && args.length >= 4) { saveHere(args[3]); return true; }
        if (sub.equals("goto") && args.length >= 4) { gotoWp(args[3]); return true; }
        if ((sub.equals("del") || sub.equals("remove")) && args.length >= 4) { remove(args[3]); return true; }
        msg("§f.jay wp save|goto|list|del <name>");
        return true;
    }

    public static void save(String name, BlockPos pos) {
        if (name == null || name.isBlank() || pos == null) return;
        String key = name.toLowerCase().trim().replace(' ', '_');
        POINTS.put(key, pos.toImmutable());
        msg("§aWaypoint §f" + key + " §7@ " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        try {
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        } catch (Throwable ignored) {}
    }

    public static void saveHere(String name) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return;
        save(name, mc.player.getBlockPos());
    }

    public static void gotoWp(String name) {
        if (name == null) return;
        BlockPos p = POINTS.get(name.toLowerCase().trim());
        if (p == null) {
            msg("§cNot found");
            return;
        }
        try {
            if (BaritoneCompat.pathTo(p)) {
                msg("§aPathing to §f" + name);
            } else {
                msg("§cBaritone unavailable");
            }
        } catch (Throwable t) {
            msg("§cBaritone unavailable");
        }
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
        if (name == null) return;
        if (POINTS.remove(name.toLowerCase().trim()) != null) {
            msg("§cRemoved §f" + name);
            try {
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            } catch (Throwable ignored) {}
        } else msg("§cNot found");
    }

    public static void loadFromConfig(String name, int x, int y, int z) {
        if (name == null || name.isBlank()) return;
        POINTS.put(name.toLowerCase(), new BlockPos(x, y, z));
    }

    public static void loadLine(String k, String v) {
        if (k == null || v == null) return;
        String name = k.startsWith("wp.") ? k.substring(3) : k;
        if (name.isBlank()) return;
        try {
            String[] p = v.split(",");
            if (p.length < 3) return;
            int x = Integer.parseInt(p[0].trim());
            int y = Integer.parseInt(p[1].trim());
            int z = Integer.parseInt(p[2].trim());
            loadFromConfig(name, x, y, z);
        } catch (Exception ignored) {}
    }

    public static void writeConfig(StringBuilder sb) {
        for (var e : POINTS.entrySet()) {
            BlockPos p = e.getValue();
            sb.append("wp.").append(e.getKey()).append('=')
                    .append(p.getX()).append(',').append(p.getY()).append(',').append(p.getZ()).append('\n');
        }
    }

    private static void msg(String s) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }

    @Override
    public void onTick() {}
}
