package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;

public class Hitboxes extends Module {

    public static double expand = 0.10;

    public Hitboxes() {
        super("Hitboxes", "Small expand — keep low", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        expand = ClientSettings.hitboxExpand;
    }

    public static void setExpand(double value) {
        expand = Math.max(0.0, Math.min(0.45, value));
        ClientSettings.hitboxExpand = expand;
    }
}
