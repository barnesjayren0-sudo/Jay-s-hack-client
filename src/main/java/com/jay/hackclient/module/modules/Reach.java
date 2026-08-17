package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class Reach extends Module {

    public static final double EXTRA = 0.5;

    public Reach() {
        super("Reach", "Extra reach helper (mixin later)", Category.COMBAT);
    }

    public static boolean isActive() {
        // Used by future mixins
        return com.jay.hackclient.JayHackClient.moduleManager != null
                && com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Reach") != null
                && com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("Reach").isEnabled();
    }
}
