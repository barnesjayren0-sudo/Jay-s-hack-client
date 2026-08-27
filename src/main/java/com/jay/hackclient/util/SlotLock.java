package com.jay.hackclient.util;

/**
 * Only one system owns hotbar selection at a time.
 * Higher priority can steal from lower priority.
 *
 * Priority guide:
 *   ShieldBreak = 30
 *   PotRefill   = 20
 *   AutoSword   = 10
 */
public final class SlotLock {

    private static String owner = null;
    private static long until = 0;
    private static int priority = 0;

    private SlotLock() {}

    public static boolean tryAcquire(String module, long ms) {
        return tryAcquire(module, ms, 10);
    }

    public static boolean tryAcquire(String module, long ms, int prio) {
        long now = System.currentTimeMillis();
        if (owner != null && !owner.equals(module) && now < until) {
            // Higher priority may steal
            if (prio <= priority) return false;
        }
        owner = module;
        until = now + ms;
        priority = prio;
        return true;
    }

    public static boolean isHeldBy(String module) {
        return module.equals(owner) && System.currentTimeMillis() < until;
    }

    public static void release(String module) {
        if (module.equals(owner)) {
            owner = null;
            until = 0;
            priority = 0;
        }
    }

    public static boolean isLockedByOther(String module) {
        long now = System.currentTimeMillis();
        return owner != null && !owner.equals(module) && now < until;
    }

    public static String currentOwner() {
        if (System.currentTimeMillis() >= until) return null;
        return owner;
    }
}
