package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

/** Higher step height — walk up full blocks. */
public class Step extends Module {

    public final NumberSetting height = new NumberSetting("Height", "Step height", 1.0, 0.6, 2.5, 0.1);
    private float oldStep = 0.6f;

    public Step() {
        super("Step", "Walk up blocks without jumping", Category.ANARCHY);
        addSetting(height);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            try {
                oldStep = mc.player.getStepHeight();
            } catch (Throwable ignored) {
                oldStep = 0.6f;
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            try {
                mc.player.setStepHeight(oldStep);
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        try {
            mc.player.setStepHeight(height.getFloat());
        } catch (Throwable ignored) {}
    }
}
