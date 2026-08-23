package com.jay.hackclient.compat;

import net.minecraft.util.math.BlockPos;

/**
 * Polished runtime bridge to official Baritone (baritone-fabric-1.21.11).
 * Soft-fail everything — never crash the client if Baritone is missing or API shifts.
 */
public final class BaritoneCompat {

    private static Boolean present = null;
    private static String lastError = null;
    private static String lastAction = "idle";
    private static long lastActionMs = 0;

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
        if (t.getMessage() != null && !t.getMessage().isEmpty()) {
            lastError += ": " + t.getMessage();
        }
        if (lastError.length() > 80) lastError = lastError.substring(0, 80);
    }

    private static Object primary() throws Exception {
        Class<?> api = Class.forName("baritone.api.BaritoneAPI");
        Object provider = api.getMethod("getProvider").invoke(null);
        if (provider == null) throw new IllegalStateException("no provider");
        Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
        if (baritone == null) throw new IllegalStateException("no primary baritone");
        return baritone;
    }

    private static Object invoke(Object target, String method, Class<?>[] types, Object... args) throws Exception {
        return target.getClass().getMethod(method, types).invoke(target, args);
    }

    private static Object invoke0(Object target, String method) throws Exception {
        return target.getClass().getMethod(method).invoke(target);
    }

    private static Class<?> goalIface() throws ClassNotFoundException {
        return Class.forName("baritone.api.pathing.goals.Goal");
    }

    private static Object goalBlock(int x, int y, int z) throws Exception {
        return Class.forName("baritone.api.pathing.goals.GoalBlock")
                .getConstructor(int.class, int.class, int.class)
                .newInstance(x, y, z);
    }

    private static Object goalXZ(int x, int z) throws Exception {
        return Class.forName("baritone.api.pathing.goals.GoalXZ")
                .getConstructor(int.class, int.class)
                .newInstance(x, z);
    }

    private static boolean setGoalAndPath(Object goal) throws Exception {
        Object baritone = primary();
        Object process = invoke0(baritone, "getCustomGoalProcess");
        process.getClass().getMethod("setGoalAndPath", goalIface()).invoke(process, goal);
        return true;
    }

    public static boolean pathTo(BlockPos pos) {
        return pos != null && pathTo(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean pathTo(int x, int y, int z) {
        if (!isPresent()) {
            lastError = "Baritone not installed";
            return false;
        }
        try {
            setGoalAndPath(goalBlock(x, y, z));
            ok("goto " + x + " " + y + " " + z);
            return true;
        } catch (Throwable t) {
            fail(t);
            return false;
        }
    }

    public static boolean pathToXZ(int x, int z) {
        if (!isPresent()) {
            lastError = "Baritone not installed";
            return false;
        }
        try {
            setGoalAndPath(goalXZ(x, z));
            ok("gotoXZ " + x + " " + z);
            return true;
        } catch (Throwable t) {
            fail(t);
            return false;
        }
    }

    public static void cancel() {
        if (!isPresent()) return;
        try {
            Object pathing = invoke0(primary(), "getPathingBehavior");
            try {
                invoke0(pathing, "cancelEverything");
            } catch (NoSuchMethodException e) {
                try {
                    Object process = invoke0(primary(), "getCustomGoalProcess");
                    process.getClass().getMethod("setGoalAndPath", goalIface())
                            .invoke(process, new Object[]{null});
                } catch (Throwable ignored) {}
            }
            ok("stopped");
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static void cancelPath() {
        cancel();
    }

    public static void pause() {
        if (!isPresent()) return;
        try {
            Object pathing = invoke0(primary(), "getPathingBehavior");
            try {
                invoke0(pathing, "requestPause");
            } catch (NoSuchMethodException e) {
                // soft ignore
            }
            ok("paused");
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static void resume() {
        if (!isPresent()) return;
        try {
            Object pathing = invoke0(primary(), "getPathingBehavior");
            for (String name : new String[]{"requestResume", "secretInternalSetGoalAndPath"}) {
                try {
                    invoke0(pathing, name);
                    ok("resumed");
                    return;
                } catch (NoSuchMethodException ignored) {}
            }
            ok("resume-attempted");
        } catch (Throwable t) {
            fail(t);
        }
    }

    public static boolean mine(String... blockNames) {
        if (!isPresent()) {
            lastError = "Baritone not installed";
            return false;
        }
        if (blockNames == null || blockNames.length == 0) {
            lastError = "no blocks";
            return false;
        }
        // normalize ids
        String[] names = new String[blockNames.length];
        for (int i = 0; i < blockNames.length; i++) {
            String n = blockNames[i].toLowerCase().replace("minecraft:", "");
            names[i] = n;
        }
        try {
            Object mine = invoke0(primary(), "getMineProcess");
            // Prefer mineByName(int, String...)
            try {
                mine.getClass().getMethod("mineByName", int.class, String[].class)
                        .invoke(mine, 0, names);
                ok("mine " + String.join(",", names));
                return true;
            } catch (NoSuchMethodException e) {
                mine.getClass().getMethod("mineByName", String[].class)
                        .invoke(mine, (Object) names);
                ok("mine " + String.join(",", names));
                return true;
            }
        } catch (Throwable t) {
            fail(t);
            return false;
        }
    }

    public static boolean isPathing() {
        if (!isPresent()) return false;
        try {
            Object pathing = invoke0(primary(), "getPathingBehavior");
            Object r = invoke0(pathing, "isPathing");
            return Boolean.TRUE.equals(r);
        } catch (Throwable t) {
            return false;
        }
    }

    public static String status() {
        if (!isPresent()) return "offline";
        if (isPathing()) return "pathing";
        if ("stopped".equals(lastAction) || "idle".equals(lastAction)) return lastAction;
        if (System.currentTimeMillis() - lastActionMs < 2000) return lastAction;
        return "idle";
    }

    /** Short HUD line */
    public static String hudLine() {
        if (!isPresent()) return null;
        String s = status();
        if ("idle".equals(s) || "stopped".equals(s) || "offline".equals(s)) return null;
        return s;
    }
}
