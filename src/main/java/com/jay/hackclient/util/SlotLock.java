package com.jay.hackclient.util;

/**
 * Only one system owns hotbar/offhand swaps at a time.
 * Higher priority steals from lower.
 *
 * Priority guide:
 *   AutoTotem (critical) = 95
 *   AutoTotem (normal)   = 40
 *   ShieldBreak          = 50
 *   PotRefill / AutoPot  = 35
 *   InvManager           = 20
 *   AutoSword            = 10
 */
public final class SlotLock {

    public static final int PRIO_TOTEM_CRIT = 95;
    public static final int PRIO_SHIELD = 50;
    public static final int PRIO_TOTEM = 40;
    public static final int PRIO_POT = 35;
    public static final int PRIO_INV = 20;
    public static final int PRIO_SWORD = 10;

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
        if (module != null && module.equals(owner)) {
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
        if (System.currentTimeMillis() >= until) {
            owner = null;
            priority = 0;
            return null;
        }
        return owner;
    }
}
