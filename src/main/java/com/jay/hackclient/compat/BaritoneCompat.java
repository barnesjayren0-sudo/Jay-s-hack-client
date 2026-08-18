package com.jay.hackclient.compat;

import net.minecraft.util.math.BlockPos;

/**
 * Optional Baritone hook via reflection — no hard dependency.
 * Install Baritone as a separate Fabric mod; we detect it at runtime.
 */
public final class BaritoneCompat {

    private static Boolean present = null;

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

    /** Path to a block if Baritone is loaded. Returns false if unavailable. */
    public static boolean pathTo(BlockPos pos) {
        if (!isPresent()) return false;
        try {
            Class<?> api = Class.forName("baritone.api.BaritoneAPI");
            Object provider = api.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object goalProcess = baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
            Class<?> goalBlock = Class.forName("baritone.api.pathing.goals.GoalBlock");
            Object goal = goalBlock.getConstructor(int.class, int.class, int.class)
                    .newInstance(pos.getX(), pos.getY(), pos.getZ());
            goalProcess.getClass().getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal"))
                    .invoke(goalProcess, goal);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void cancelPath() {
        if (!isPresent()) return;
        try {
            Class<?> api = Class.forName("baritone.api.BaritoneAPI");
            Object provider = api.getMethod("getProvider").invoke(null);
            Object baritone = provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
            Object goalProcess = baritone.getClass().getMethod("getCustomGoalProcess").invoke(baritone);
            goalProcess.getClass().getMethod("setGoalAndPath", Class.forName("baritone.api.pathing.goals.Goal"))
                    .invoke(goalProcess, new Object[]{null});
        } catch (Throwable ignored) {
        }
    }
}
