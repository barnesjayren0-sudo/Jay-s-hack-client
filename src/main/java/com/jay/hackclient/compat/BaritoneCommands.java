package com.jay.hackclient.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/** Polished # and .b command parser for JayBaritone. */
public final class BaritoneCommands {

    private static long lastMsgMs;

    private BaritoneCommands() {}

    public static boolean tryHandle(String message) {
        if (message == null) return false;
        String m = message.trim();
        if (m.startsWith("#")) {
            return dispatch(m.substring(1).trim());
        }
        if (m.startsWith(".b") || m.startsWith(".B")) {
            String rest = m.length() <= 2 ? "help" : m.substring(2).trim();
            if (rest.isEmpty()) rest = "help";
            return dispatch(rest);
        }
        return false;
    }

    private static boolean dispatch(String body) {
        if (body.isEmpty()) {
            help();
            return true;
        }
        String[] args = body.split("\\s+");
        String cmd = args[0].toLowerCase();
        MinecraftClient mc = MinecraftClient.getInstance();

        switch (cmd) {
            case "help", "?", "h" -> help();
            case "status", "s" -> {
                if (!BaritoneCompat.isPresent()) {
                    chat("§cBaritone offline §7— put §fbaritone-fabric-1.21.11.jar §7in mods/");
                } else {
                    chat("§aBaritone §f" + BaritoneCompat.status()
                            + (BaritoneCompat.isPathing() ? " §a●" : " §8○"));
                }
            }
            case "stop", "cancel", "forcecancel", "clear" -> {
                BaritoneCompat.cancel();
                chat("§cPath cancelled");
            }
            case "pause" -> {
                BaritoneCompat.pause();
                chat("§ePaused");
            }
            case "resume", "start" -> {
                // #start = resume pathing if possible
                BaritoneCompat.resume();
                chat("§aResume / start");
            }
            case "goto", "goal", "path", "gp" -> handleGoto(args, mc);
            case "mine" -> handleMine(args);
            case "thisway", "tw" -> handleThisway(args, mc);
            default -> chat("§cUnknown §f#" + cmd + " §7— §f#help");
        }
        return true;
    }

    private static void handleGoto(String[] args, MinecraftClient mc) {
        if (!BaritoneCompat.isPresent()) {
            chat("§cInstall §fbaritone-fabric-1.21.11.jar §cin mods/");
            return;
        }
        if (args.length == 3) {
            int x = parse(args[1], 0);
            int z = parse(args[2], 0);
            boolean ok = BaritoneCompat.pathToXZ(x, z);
            chat(ok ? "§aPath → §f" + x + " ~ " + z : "§c" + safeErr());
        } else if (args.length >= 4) {
            int x = parse(args[1], 0);
            int y = parse(args[2], 64);
            int z = parse(args[3], 0);
            boolean ok = BaritoneCompat.pathTo(x, y, z);
            chat(ok ? "§aPath → §f" + x + " " + y + " " + z : "§c" + safeErr());
        } else {
            chat("§f#goto <x> <z> §7or §f#goto <x> <y> <z>");
        }
    }

    private static void handleMine(String[] args) {
        if (!BaritoneCompat.isPresent()) {
            chat("§cInstall Baritone jar first");
            return;
        }
        if (args.length < 2) {
            chat("§f#mine iron_ore §7| §f#mine diamond_ore coal_ore");
            return;
        }
        String[] blocks = new String[args.length - 1];
        System.arraycopy(args, 1, blocks, 0, blocks.length);
        boolean ok = BaritoneCompat.mine(blocks);
        chat(ok ? "§aMining §f" + String.join(", ", blocks) : "§c" + safeErr());
    }

    private static void handleThisway(String[] args, MinecraftClient mc) {
        if (mc.player == null) return;
        if (!BaritoneCompat.isPresent()) {
            chat("§cBaritone offline");
            return;
        }
        int dist = args.length >= 2 ? parse(args[1], 100) : 100;
        dist = Math.max(8, Math.min(5000, dist));
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        int x = mc.player.getBlockX() + (int) Math.round(-Math.sin(rad) * dist);
        int z = mc.player.getBlockZ() + (int) Math.round(Math.cos(rad) * dist);
        boolean ok = BaritoneCompat.pathToXZ(x, z);
        chat(ok ? "§aThisway §f" + dist + "m" : "§c" + safeErr());
    }

    private static String safeErr() {
        String e = BaritoneCompat.lastError();
        return e == null || e.isEmpty() ? "failed" : e;
    }

    private static int parse(String s, int def) {
        try {
            return Integer.parseInt(s.replace("~", "").trim());
        } catch (Exception e) {
            return def;
        }
    }

    private static void help() {
        chat("§bJayBaritone §8· needs Baritone jar");
        chat("§f#goto x y z  §8|  §f#mine iron_ore");
        chat("§f#stop  #pause  #resume  #thisway 80");
        chat("§f#status  §8|  §f.b goto …");
    }

    private static void chat(String s) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastMsgMs < 80) {
            // still allow, just slight gate against flood loops
        }
        lastMsgMs = now;
        mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }
}
