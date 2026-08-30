package com.jay.hackclient;

import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;

/** Extra module registration for v1.41 features. */
public final class ModuleBootstrap {
    private ModuleBootstrap() {}
    public static void registerExtra(ModuleManager mm) {
        if (mm == null) return;
        mm.register(new PlayerBoxes());
        mm.register(new PearlTrajectory());
        mm.register(new CombatHUD());
        mm.register(new Waypoints());
    }
}
