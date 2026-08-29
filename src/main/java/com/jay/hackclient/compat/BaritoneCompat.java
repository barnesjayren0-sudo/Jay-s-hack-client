package com.jay.hackclient.compat;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

/**
 * Runtime bridge to official Baritone (baritone-fabric-1.21.11).
 * Soft-fail everything — never crash if Baritone is missing.
 */
public final class BaritoneCompat {

    private static Boolean present = null;
    private static String lastError = null;
    private static String lastAction = "idle";
    private static long lastActionMs = 0;
    private static long combatPauseUntil;
    private static boolean pathingHint;

    private BaritoneCompat() {}

    public static boolean isPresent() {
        if (present == null) {
            try {
                Class.forName("baritone.api.BaritoneAPI", false, BaritoneCompat.class.getClassLoader());
                present = true;
            } catch (Throwable t) {
                present = false;
            }
        }
        return Boolean.TRUE.equals(present);
    }

    public static void resetDetection() {
        present = null;
        lastError = null;
    }

    public static String lastError() {
        return lastError == null ? "" : lastError;
    }

    public static String lastAction() {
        return lastAction;
    }

    private static void ok(String action) {
        lastError = null;
        lastAction = action;
        lastActionMs = System.currentTimeMillis();
    }

    private static void fail(Throwable t) {
        lastError = t.getClass().getSimpleName();
    }

    public static void cancel() {
        pathingHint = false;
        if (!isPresent()) return;
        try {
            Object provider = Class.forName("baritone.api.BaritoneAPI")
                    .getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object behavior = baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
            behavior.getClass().getMethod("cancelEverything").invoke(behavior);
            ok("stopped");
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static void pauseCombat() {
        long now = System.currentTimeMillis();
        if (now < combatPauseUntil) return;
        combatPauseUntil = now + 600;
        try {
            cancel();
            lastAction = "combat-pause";
            lastActionMs = now;
        } catch (Throwable ignored) {}
    }

    public static void pause() {
        cancel();
        ok("paused");
    }

    public static void resume() {
        ok("resumed");
        // Baritone has no universal resume; user re-issues goal
    }

    public static boolean isPathing() {
        if (!isPresent()) return false;
        try {
            Object provider = Class.forName("baritone.api.BaritoneAPI")
                    .getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object behavior = baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
            Object path = behavior.getClass().getMethod("isPathing").invoke(behavior);
            if (path instanceof Boolean b) {
                pathingHint = b;
                return b;
            }
        } catch (Throwable ignored) {}
        return pathingHint && System.currentTimeMillis() - lastActionMs < 30000
                && !"stopped".equals(lastAction) && !"idle".equals(lastAction);
    }

    public static boolean pathTo(BlockPos pos) {
        if (!isPresent() || pos == null) return false;
        try {
            Object provider = Class.forName("baritone.api.BaritoneAPI")
                    .getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object goalProcess = baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
            Class<?> goalBlock = Class.forName("baritone.api.pathing.goals.GoalBlock");
            Object goal = goalBlock.getConstructor(int.class, int.class, int.class)
                    .newInstance(pos.getX(), pos.getY(), pos.getZ());
            goalProcess.getClass().getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal"))
                    .invoke(goalProcess, goal);
            pathingHint = true;
            ok("goto " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
            return true;
        } catch (Throwable t) {
            fail(t);
            return false;
        }
    }

    public static boolean pathTo(int x, int y, int z) {
        return pathTo(new BlockPos(x, y, z));
    }

    public static boolean pathToXZ(int x, int z) {
        int y = 64;
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) y = (int) Math.floor(mc.player.getY());
        } catch (Throwable ignored) {}
        return pathTo(new BlockPos(x, y, z));
    }

    public static boolean mine(String[] blocks) {
        if (blocks == null || blocks.length == 0) return false;
        StringBuilder sb = new StringBuilder("mine");
        for (String b : blocks) {
            if (b == null || b.isBlank()) continue;
            sb.append(' ').append(b.trim());
        }
        return executeCommand(sb.toString());
    }

    public static boolean executeCommand(String cmd) {
        if (!isPresent() || cmd == null || cmd.isBlank()) return false;
        try {
            Object provider = Class.forName("baritone.api.BaritoneAPI")
                    .getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object cmdMgr = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            String c = cmd.startsWith("#") ? cmd.substring(1).trim() : cmd.trim();
            cmdMgr.getClass().getMethod("execute", String.class).invoke(cmdMgr, c);
            pathingHint = true;
            ok(c);
            return true;
        } catch (Throwable t) {
            fail(t);
            return false;
        }
    }

    public static String status() {
        if (!isPresent()) return "Baritone not present";
        return lastAction + (lastError != null && !lastError.isEmpty() ? " err=" + lastError : "");
    }

    public static String hudLine() {
        if (!isPresent()) return null;
        if ("stopped".equals(lastAction) || "idle".equals(lastAction)) return lastAction;
        if (System.currentTimeMillis() - lastActionMs < 8000) return lastAction;
        return null;
    }
}
