package com.jay.hackclient.util;

/** Only one system owns hotbar selection at a time. */
public final class SlotLock {

    private static String owner = null;
    private static long until = 0;

    private SlotLock() {}

    public static boolean tryAcquire(String module, long ms) {
        long now = System.currentTimeMillis();
        if (owner != null && !owner.equals(module) && now < until) return false;
        owner = module;
        until = now + ms;
        return true;
    }

    public static boolean isHeldBy(String module) {
        return module.equals(owner) && System.currentTimeMillis() < until;
    }

    public static void release(String module) {
        if (module.equals(owner)) {
            owner = null;
            until = 0;
        }
    }

    public static boolean isLockedByOther(String module) {
        long now = System.currentTimeMillis();
        return owner != null && !owner.equals(module) && now < until;
    }
}
