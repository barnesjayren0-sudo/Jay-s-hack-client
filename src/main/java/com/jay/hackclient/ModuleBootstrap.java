package com.jay.hackclient;

import com.jay.hackclient.kotlin.KotlinBootstrap;
import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.AttributeSwap;
import com.jay.hackclient.module.modules.BackTrack;
import com.jay.hackclient.module.modules.CombatHUD;
import com.jay.hackclient.module.modules.HandAnimation;
import com.jay.hackclient.module.modules.PlayerBoxes;
import com.jay.hackclient.module.modules.SprintReset;
import com.jay.hackclient.module.modules.Waypoints;

/** Extra module registration (Java + Kotlin). Deduplicated with JayHackClient's core list. */
public final class ModuleBootstrap {
    private ModuleBootstrap() {}

    public static void registerExtra(ModuleManager mm) {
        if (mm == null) return;
        tryRegister(mm, "PlayerBoxes", PlayerBoxes::new);
        tryRegister(mm, "PearlTrajectory", com.jay.hackclient.module.modules.PearlTrajectory::new);
        tryRegister(mm, "CombatHUD", CombatHUD::new);
        tryRegister(mm, "Waypoints", Waypoints::new);
        tryRegister(mm, "HandAnimation", HandAnimation::new);
        tryRegister(mm, "SprintReset", SprintReset::new);
        tryRegister(mm, "AttributeSwap", AttributeSwap::new);
        tryRegister(mm, "BackTrack", BackTrack::new);
        // Fabric Language Kotlin modules
        try {
            KotlinBootstrap.register(mm);
        } catch (Throwable t) {
            System.err.println("[Jay] kotlin bootstrap: " + t.getMessage());
        }
    }

    private static void tryRegister(ModuleManager mm, String name, java.util.function.Supplier<com.jay.hackclient.module.Module> factory) {
        try {
            if (mm.getModuleByName(name) == null) {
                mm.register(factory.get());
            }
        } catch (Throwable t) {
            System.err.println("[Jay] register " + name + ": " + t.getMessage());
        }
    }
}
