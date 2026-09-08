package com.jay.hackclient.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;

/**
 * Lightweight tick-based sequencer inspired by LiquidBounce-style
 * delayed actions (wait N ticks then run).
 */
public final class Sequence {

    private static final List<Task> TASKS = new ArrayList<>();

    private Sequence() {}

    public static void tick() {
        if (TASKS.isEmpty()) return;
        Iterator<Task> it = TASKS.iterator();
        while (it.hasNext()) {
            Task t = it.next();
            if (t.cancelled) {
                it.remove();
                continue;
            }
            if (t.condition != null && !t.condition.getAsBoolean()) {
                it.remove();
                continue;
            }
            if (t.ticksLeft > 0) {
                t.ticksLeft--;
                continue;
            }
            try {
                t.action.run();
            } catch (Throwable ignored) {
            }
            it.remove();
        }
    }

    public static void run(int delayTicks, Runnable action) {
        if (action == null) return;
        Task t = new Task();
        t.ticksLeft = Math.max(0, delayTicks);
        t.action = action;
        TASKS.add(t);
    }

    public static void runWhen(int delayTicks, BooleanSupplier stillValid, Runnable action) {
        if (action == null) return;
        Task t = new Task();
        t.ticksLeft = Math.max(0, delayTicks);
        t.condition = stillValid;
        t.action = action;
        TASKS.add(t);
    }

    public static void clear() {
        TASKS.clear();
    }

    private static final class Task {
        int ticksLeft;
        BooleanSupplier condition;
        Runnable action;
        boolean cancelled;
    }
}
