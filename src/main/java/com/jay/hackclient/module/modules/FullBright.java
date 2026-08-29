package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

/** Raise gamma for visibility. */
public class FullBright extends Module {

    public final NumberSetting gamma = new NumberSetting("Gamma", "Brightness", 16.0, 1.0, 16.0, 0.5);
    private double prevGamma = 1.0;

    public FullBright() {
        super("FullBright", "Full brightness", Category.RENDER);
        addSetting(gamma);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            try {
                prevGamma = mc.options.getGamma().getValue();
                mc.options.getGamma().setValue(gamma.get());
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onTick() {
        if (mc.options == null) return;
        try {
            double g = gamma.get();
            if (Math.abs(mc.options.getGamma().getValue() - g) > 0.05) {
                mc.options.getGamma().setValue(g);
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            try { mc.options.getGamma().setValue(prevGamma); } catch (Throwable ignored) {}
        }
    }
}
