package com.jay.hackclient.module;

import com.jay.hackclient.module.modules.AttributeSwap;
import com.jay.hackclient.module.modules.BackTrack;
import com.jay.hackclient.module.modules.HandAnimation;
import com.jay.hackclient.module.modules.SprintReset;

/** Extra module registration (called from JayHackClient). */
public final class ModuleBootstrap {
    private ModuleBootstrap() {}

    public static void registerExtra(ModuleManager mm) {
        if (mm == null) return;
        tryRegister(mm, "AttributeSwap", () -> new AttributeSwap());
        tryRegister(mm, "HandAnimation", () -> new HandAnimation());
        tryRegister(mm, "SprintReset", () -> new SprintReset());
        tryRegister(mm, "BackTrack", () -> new BackTrack());
    }

    private static void tryRegister(ModuleManager mm, String name, java.util.function.Supplier<Module> factory) {
        try {
            if (mm.getModuleByName(name) == null) {
                mm.register(factory.get());
            }
        } catch (Throwable ignored) {}
    }
}
