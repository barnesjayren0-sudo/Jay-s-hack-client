package com.jay.hackclient.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Lightweight Orbit-inspired event bus (no external Meteor dependency).
 * Keeps Termux builds working while giving Meteor-style subscribe/post.
 */
public final class EventBus {

    private final Map<Class<?>, List<Consumer<?>>> listeners = new ConcurrentHashMap<>();

    public <T> void subscribe(Class<T> type, Consumer<T> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        List<Consumer<?>> list = listeners.get(event.getClass());
        if (list == null) return;
        for (Consumer<?> raw : list) {
            ((Consumer<T>) raw).accept(event);
        }
    }

    public void unsubscribeAll() {
        listeners.clear();
    }
}
