package com.jay.hackclient.compat;

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

    /** Soft-pause pathing while taking damage. */
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

    public static void pathTo(BlockPos pos) {
        if (!isPresent() || pos == null) return;
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
            ok("goto " + pos.getX() + " " + pos.getY() + " " + pos.getZ());
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static void executeCommand(String cmd) {
        if (!isPresent() || cmd == null || cmd.isBlank()) return;
        try {
            Object provider = Class.forName("baritone.api.BaritoneAPI")
                    .getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object cmdMgr = baritone.getClass().getMethod("getCommandManager").invoke(baritone);
            String c = cmd.startsWith("#") ? cmd.substring(1).trim() : cmd.trim();
            cmdMgr.getClass().getMethod("execute", String.class).invoke(cmdMgr, c);
            ok(c);
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static String status() {
        if (!isPresent()) return "Baritone not present";
        return lastAction + (lastError != null ? " err=" + lastError : "");
    }

    public static String hudLine() {
        if (!isPresent()) return null;
        if ("stopped".equals(lastAction) || "idle".equals(lastAction)) return lastAction;
        if (System.currentTimeMillis() - lastActionMs < 8000) return lastAction;
        return null;
    }
}
