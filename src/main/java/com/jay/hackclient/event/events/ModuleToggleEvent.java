package com.jay.hackclient.event.events;

import com.jay.hackclient.module.Module;

/** Fired when a module enables or disables (LiquidBounce-style activation bus). */
public final class ModuleToggleEvent {
    public final Module module;
    public final boolean enabled;

    public ModuleToggleEvent(Module module, boolean enabled) {
        this.module = module;
        this.enabled = enabled;
    }
}
