package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

/** Keep expand low. 0.08–0.15 preferred on AC. */
public class Hitboxes extends Module {

    public static double expand = 0.12;

    public Hitboxes() {
        super("Hitboxes", "Slight hitbox expand — keep low", Category.COMBAT);
    }

    public static void setExpand(double value) {
        expand = Math.max(0.0, Math.min(0.45, value));
    }
}
