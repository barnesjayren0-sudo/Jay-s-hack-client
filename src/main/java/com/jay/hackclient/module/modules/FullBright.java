package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class FullBright extends Module {

    private double oldGamma = 1.0;

    public FullBright() {
        super("FullBright", "Maximum brightness", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            oldGamma = mc.options.getGamma().getValue();
            mc.options.getGamma().setValue(16.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            mc.options.getGamma().setValue(oldGamma);
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null && isEnabled()) {
            if (mc.options.getGamma().getValue() < 15.0) {
                mc.options.getGamma().setValue(16.0);
            }
        }
    }
}
