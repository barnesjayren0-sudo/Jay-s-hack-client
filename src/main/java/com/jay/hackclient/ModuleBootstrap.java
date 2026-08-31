package com.jay.hackclient;

import com.jay.hackclient.kotlin.KotlinBootstrap;
import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;

/** Extra module registration (Java + Kotlin). */
public final class ModuleBootstrap {
    private ModuleBootstrap() {}

    public static void registerExtra(ModuleManager mm) {
        if (mm == null) return;
        try {
            mm.register(new PlayerBoxes());
            mm.register(new PearlTrajectory());
            mm.register(new CombatHUD());
            mm.register(new Waypoints());
        } catch (Throwable t) {
            System.err.println("[Jay] extra modules: " + t.getMessage());
        }
        // Fabric Language Kotlin modules
        try {
            KotlinBootstrap.register(mm);
        } catch (Throwable t) {
            System.err.println("[Jay] kotlin bootstrap: " + t.getMessage());
        }
    }
}
