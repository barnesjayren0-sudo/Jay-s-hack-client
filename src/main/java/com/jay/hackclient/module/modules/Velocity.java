package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class Velocity extends Module {

    public Velocity() {
        super("Velocity", "Reduces or cancels knockback", Category.COMBAT);
    }

    // Real implementation requires mixin on Entity velocity or packet receiving.
    // Placeholder for now.
}
