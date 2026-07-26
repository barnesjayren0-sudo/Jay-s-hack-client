package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class Reach extends Module {

    public Reach() {
        super("Reach", "Extends attack reach for sword PvP", Category.COMBAT);
    }

    // Note: Real reach modification requires mixin on GameRenderer or PlayerEntity interaction distance.
    // This is a placeholder that will be expanded with mixins.

    @Override
    public void onEnable() {
        // Future: increase reach value via mixin
    }

    @Override
    public void onDisable() {
        // Future: restore original reach
    }
}
