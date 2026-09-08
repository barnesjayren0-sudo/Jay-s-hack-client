package com.jay.client18.module.modules;

import com.jay.client18.module.Module;

public class FullBright extends Module {

    private float oldGamma = 1f;

    public FullBright() {
        super("FullBright", "Gamma boost", Category.RENDER);
    }

    @Override
    public void onEnable() {
        oldGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 12f;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = oldGamma;
    }

    @Override
    public void onTick() {
        // Keep stable without spamming option writes every tick if already set
        if (mc.gameSettings.gammaSetting < 10f) {
            mc.gameSettings.gammaSetting = 12f;
        }
    }
}
