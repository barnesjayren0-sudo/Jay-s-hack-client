package com.jay.hackclient.module;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.modules.AttributeSwap;

/** Ensures new modules are registered even if JayHackClient list is stale. */
public final class ModuleBootstrap {
    private ModuleBootstrap() {}
    public static void ensureExtras() {
        try {
            if (JayHackClient.moduleManager == null) return;
            if (JayHackClient.moduleManager.getModuleByName("AttributeSwap") == null) {
                JayHackClient.moduleManager.register(new AttributeSwap());
            }
        } catch (Throwable ignored) {}
    }
}
