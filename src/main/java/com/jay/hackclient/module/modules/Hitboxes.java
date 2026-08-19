package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class Hitboxes extends Module {

    public static double expand = 0.14;

    public Hitboxes() {
        super("Hitboxes", "Slight enemy hitbox expand", Category.COMBAT);
    }

    public static void setExpand(double value) {
        expand = Math.max(0.0, Math.min(0.5, value));
    }
}
