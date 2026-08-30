package com.jay.hackclient.util;

/**
 * One rotation owner per short window so KillAura and AimAssist do not fight the camera.
 * Priority: KillAura > AimAssist > other.
 */
public final class RotationOwner {

    private static String owner = null;
    private static int priority = 0;
    private static long untilMs = 0;

    private RotationOwner() {}

    /** KillAura = 2, AimAssist = 1, other = 0 */
    public static boolean tryClaim(String name, int prio, int holdMs) {
        long now = System.currentTimeMillis();
        if (owner != null && now < untilMs) {
            if (!owner.equals(name) && prio < priority) return false;
        }
        owner = name;
        priority = prio;
        untilMs = now + Math.max(16, holdMs);
        return true;
    }

    public static boolean canRotate(String name) {
        long now = System.currentTimeMillis();
        if (owner == null || now >= untilMs) return true;
        return owner.equals(name);
    }

    public static void release(String name) {
        if (name != null && name.equals(owner)) {
            owner = null;
            priority = 0;
            untilMs = 0;
        }
    }

    public static void clear() {
        owner = null;
        priority = 0;
        untilMs = 0;
    }
}
