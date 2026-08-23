package com.jay.hackclient.compat;

import net.minecraft.util.math.BlockPos;

/**
 * JayBaritone — runtime bridge to official Baritone (baritone-fabric-1.21.11).
 * Put the Baritone jar in mods/ next to this client. No compile dependency.
 *
 * This is not a source fork of Baritone (LGPL); it is a control layer that
 * drives Baritone when present, same idea as Meteor/Rusher Baritone integration.
 */
public final class BaritoneCompat {

    private static Boolean present = null;
    private static String lastError = null;

    private BaritoneCompat() {}

    public static boolean isPresent() {
        if (present == null) {
            try {
                Class.forName("baritone.api.BaritoneAPI");
                present = true;
            } catch (ClassNotFoundException e) {
                present = false;
            }
        }
        return present;
    }

    public static String lastError() {
        return lastError;
    }

    public static void resetDetection() {
        present = null;
        lastError = null;
    }

    private static Object primary() throws Exception {
        Class<?> api = Class.forName("baritone.api.BaritoneAPI");
        Object provider = api.getMethod("getProvider").invoke(null);
        return provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
    }

    private static Object customGoalProcess(Object baritone) throws Exception {
        return baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
    }

    private static Object pathingBehavior(Object baritone) throws Exception {
        return baritone.getClass().getMethod("getPathingBehavior").invoke(baritone);
    }

    private static Object mineProcess(Object baritone) throws Exception {
        return baritone.getClass().getMethod("getMineProcess").invoke(baritone);
    }

    private static Object goalBlock(int x, int y, int z) throws Exception {
        Class<?> goalBlock = Class.forName("baritone.api.pathing.goals.GoalBlock");
        return goalBlock.getConstructor(int.class, int.class, int.class).newInstance(x, y, z);
    }

    private static Object goalXZ(int x, int z) throws Exception {
        Class<?> goalXZ = Class.forName("baritone.api.pathing.goals.GoalXZ");
        return goalXZ.getConstructor(int.class, int.class).newInstance(x, z);
    }

    private static Class<?> goalClass() throws Exception {
        return Class.forName("baritone.api.pathing.goals.Goal");
    }

    public static boolean pathTo(BlockPos pos) {
        return pathTo(pos.getX(), pos.getY(), pos.getZ());
    }

    public static boolean pathTo(int x, int y, int z) {
        if (!isPresent()) {
            lastError = "Baritone not installed";
            return false;
        }
        try {
            Object baritone = primary();
            Object goalProcess = customGoalProcess(baritone);
            Object goal = goalBlock(x, y, z);
            goalProcess.getClass().getMethod("setGoalAndPath", goalClass()).invoke(goalProcess, goal);
            lastError = null;
            return true;
        } catch (Throwable t) {
            lastError = t.getClass().getSimpleName() + ": " + t.getMessage();
            return false;
        }
    }

    public static boolean pathToXZ(int x, int z) {
        if (!isPresent()) {
            lastError = "Baritone not installed";
            return false;
        }
        try {
            Object baritone = primary();
            Object goalProcess = customGoalProcess(baritone);
            Object goal = goalXZ(x, z);
            goalProcess.getClass().getMethod("setGoalAndPath", goalClass()).invoke(goalProcess, goal);
            lastError = null;
            return true;
        } catch (Throwable t) {
            lastError = t.getClass().getSimpleName() + ": " + t.getMessage();
            return false;
        }
    }

    /** Cancel path / clear goal. */
    public static void cancel() {
        if (!isPresent()) return;
        try {
            Object baritone = primary();
            Object pathing = pathingBehavior(baritone);
            try {
                pathing.getClass().getMethod("cancelEverything").invoke(pathing);
            } catch (NoSuchMethodException e) {
                // fallback clear goal
                Object goalProcess = customGoalProcess(baritone);
                goalProcess.getClass().getMethod("setGoalAndPath", goalClass())
                        .invoke(goalProcess, new Object[]{null});
            }
            lastError = null;
        } catch (Throwable t) {
            lastError = t.getMessage();
        }
    }

    public static void cancelPath() {
        cancel();
    }

    public static void pause() {
        if (!isPresent()) return;
        try {
            Object pathing = pathingBehavior(primary());
            pathing.getClass().getMethod("requestPause").invoke(pathing);
        } catch (Throwable t) {
            lastError = t.getMessage();
        }
    }

    public static void resume() {
        if (!isPresent()) return;
        try {
            Object pathing = pathingBehavior(primary());
            try {
                pathing.getClass().getMethod("requestResume").invoke(pathing);
            } catch (NoSuchMethodException e) {
                // some builds use cancel pause flag differently
            }
        } catch (Throwable t) {
            lastError = t.getMessage();
        }
    }

    /** Mine blocks by name, e.g. "iron_ore". */
    public static boolean mine(String... blockNames) {
        if (!isPresent()) {
            lastError = "Baritone not installed";
            return false;
        }
        if (blockNames == null || blockNames.length == 0) return false;
        try {
            Object baritone = primary();
            Object mine = mineProcess(baritone);
            // mineByName(String...)
            mine.getClass().getMethod("mineByName", int.class, String[].class)
                    .invoke(mine, 0, blockNames);
            lastError = null;
            return true;
        } catch (Throwable t) {
            // try alternate signature mineByName(String...)
            try {
                Object baritone = primary();
                Object mine = mineProcess(baritone);
                mine.getClass().getMethod("mineByName", String[].class)
                        .invoke(mine, (Object) blockNames);
                lastError = null;
                return true;
            } catch (Throwable t2) {
                lastError = t2.getMessage();
                return false;
            }
        }
    }

    public static boolean isPathing() {
        if (!isPresent()) return false;
        try {
            Object pathing = pathingBehavior(primary());
            Object r = pathing.getClass().getMethod("isPathing").invoke(pathing);
            return r instanceof Boolean && (Boolean) r;
        } catch (Throwable t) {
            return false;
        }
    }

    public static String status() {
        if (!isPresent()) return "Baritone OFF (install jar in mods/)";
        try {
            boolean p = isPathing();
            return p ? "pathing" : "idle";
        } catch (Throwable t) {
            return "error";
        }
    }
}
