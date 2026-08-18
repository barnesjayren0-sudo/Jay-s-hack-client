package com.jay.hackclient.event.events;

import com.jay.hackclient.event.Event;
import net.minecraft.entity.Entity;

public class AttackEvent extends Event {
    public final Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }
}
