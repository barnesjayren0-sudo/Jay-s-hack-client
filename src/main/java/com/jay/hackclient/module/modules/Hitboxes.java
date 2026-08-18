package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

/** Expands other players' hitboxes client-side via mixin. Keep expand low on AC servers. */
public class Hitboxes extends Module {

    /** Horizontal expand in blocks. 0.1–0.25 is "quiet"; 0.5+ is blatant. */
    public static double expand = 0.18;

    public Hitboxes() {
        super("Hitboxes", "Expands enemy hitboxes (keep low)", Category.COMBAT);
    }

    public static void setExpand(double value) {
        expand = Math.max(0.0, Math.min(1.0, value));
    }
}
