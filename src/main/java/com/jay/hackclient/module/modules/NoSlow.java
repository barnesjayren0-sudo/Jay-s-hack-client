package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class NoSlow extends Module {

    public NoSlow() {
        super("NoSlow", "Removes slowdown when using items", Category.MOVEMENT);
    }

    // Requires mixin on PlayerEntity movement input.
}
