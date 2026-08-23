package com.jay.hackclient.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Chat commands for JayBaritone: #goto, #mine, #stop, .b ...
 */
public final class BaritoneCommands {

    private BaritoneCommands() {}

    /** @return true if consumed */
    public static boolean tryHandle(String message) {
        if (message == null) return false;
        String m = message.trim();
        if (m.startsWith("#")) {
            return handleHash(m.substring(1).trim());
        }
        if (m.startsWith(".b ") || m.equals(".b")) {
            String rest = m.length() > 2 ? m.substring(2).trim() : "help";
            return handleHash(rest);
        }
        return false;
    }

    private static boolean handleHash(String body) {
        if (body.isEmpty()) {
            help();
            return true;
        }
        String[] args = body.split("\\s+");
        String cmd = args[0].toLowerCase();
        MinecraftClient mc = MinecraftClient.getInstance();

        switch (cmd) {
            case "help", "?" -> help();
            case "status" -> chat("§f" + BaritoneCompat.status()
                    + (BaritoneCompat.isPresent() ? "" : " · put baritone-fabric-1.21.11.jar in mods/"));
            case "stop", "cancel", "forcecancel" -> {
                BaritoneCompat.cancel();
                chat("§cStopped");
            }
            case "pause" -> {
                BaritoneCompat.pause();
                chat("§ePaused");
            }
            case "resume" -> {
                BaritoneCompat.resume();
                chat("§aResumed");
            }
            case "goto", "goal", "path" -> handleGoto(args, mc);
            case "mine" -> {
                if (args.length < 2) chat("§f#mine iron_ore");
                else {
                    String[] blocks = new String[args.length - 1];
                    System.arraycopy(args, 1, blocks, 0, blocks.length);
                    boolean ok = BaritoneCompat.mine(blocks);
                    chat(ok ? "§aMining " + String.join(", ", blocks)
                            : "§cMine failed: " + BaritoneCompat.lastError());
                }
            }
            case "thisway" -> {
                if (mc.player == null) return true;
                int dist = args.length >= 2 ? parse(args[1], 100) : 100;
                float yaw = mc.player.getYaw();
                double rad = Math.toRadians(yaw);
                int x = mc.player.getBlockX() + (int) Math.round(-Math.sin(rad) * dist);
                int z = mc.player.getBlockZ() + (int) Math.round(Math.cos(rad) * dist);
                boolean ok = BaritoneCompat.pathToXZ(x, z);
                chat(ok ? "§aThisway " + dist : "§c" + BaritoneCompat.lastError());
            }
            default -> chat("§cUnknown · #help");
        }
        return true;
    }

    private static void handleGoto(String[] args, MinecraftClient mc) {
        if (!BaritoneCompat.isPresent()) {
            chat("§cInstall baritone-fabric-1.21.11.jar in mods/");
            return;
        }
        if (args.length == 3) {
            int x = parse(args[1], 0), z = parse(args[2], 0);
            boolean ok = BaritoneCompat.pathToXZ(x, z);
            chat(ok ? "§aGoto " + x + " " + z : "§c" + BaritoneCompat.lastError());
        } else if (args.length >= 4) {
            int x = parse(args[1], 0), y = parse(args[2], 64), z = parse(args[3], 0);
            boolean ok = BaritoneCompat.pathTo(x, y, z);
            chat(ok ? "§aGoto " + x + " " + y + " " + z : "§c" + BaritoneCompat.lastError());
        } else {
            chat("§f#goto <x> <z> | #goto <x> <y> <z>");
        }
    }

    private static int parse(String s, int def) {
        try {
            return Integer.parseInt(s.replace("~", ""));
        } catch (Exception e) {
            return def;
        }
    }

    private static void help() {
        chat("§bJayBaritone (needs Baritone jar)");
        chat("§f#goto x y z  §7|  §f#mine iron_ore");
        chat("§f#stop #pause #resume #thisway #status");
        chat("§7Also: .b goto …");
    }

    private static void chat(String s) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
        }
    }
}
