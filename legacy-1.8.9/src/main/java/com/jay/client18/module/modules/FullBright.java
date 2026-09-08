package com.jay.client18.module.modules;

import com.jay.client18.module.Module;

public class FullBright extends Module {

    private float oldGamma = 1f;

    public FullBright() {
        super("FullBright", "Max gamma", Category.RENDER);
    }

    @Override
    public void onEnable() {
        oldGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = 16f;
    }

    @Override
    public void onDisable() {
        mc.gameSettings.gammaSetting = oldGamma;
    }

    @Override
    public void onTick() {
        mc.gameSettings.gammaSetting = 16f;
    }
}
