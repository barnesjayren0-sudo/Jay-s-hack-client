package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;

/**
 * Hand Animation — slows / restyles first-person swing & equip animations.
 * Settings are read by HeldItemRenderer / LivingEntity mixins (static getters).
 * Real client-side only — no custom packets.
 */
public class HandAnimation extends Module {

    public static HandAnimation INSTANCE;

    public final ModeSetting mode = new ModeSetting(
            "Mode", "Animation style", "Slow",
            "Slow", "Smooth", "Static", "Fast", "Custom"
    );

    public final NumberSetting swingSpeed = new NumberSetting(
            "Swing Speed", "Lower = slower swing (1.0 = vanilla)", 0.45, 0.05, 2.0, 0.05
    );

    public final NumberSetting equipSpeed = new NumberSetting(
            "Equip Speed", "Item equip / switch animation speed", 0.50, 0.05, 2.0, 0.05
    );

    public final NumberSetting swingHeight = new NumberSetting(
            "Swing Height", "Vertical offset of the swing arc", 0.0, -1.0, 1.0, 0.05
    );

    public final NumberSetting swingProgress = new NumberSetting(
            "Progress Scale", "How far the arm travels during a swing", 1.0, 0.2, 1.5, 0.05
    );

    public final BoolSetting mainHandOnly = new BoolSetting(
            "Main Hand Only", "Only affect main hand", false
    );

    public final BoolSetting ignoreMine = new BoolSetting(
            "Ignore Mining", "Don't slow swing while breaking blocks", true
    );

    public HandAnimation() {
        super("HandAnimation", "Slow / restyle hand & equip animations", Category.RENDER);
        addSetting(mode);
        addSetting(swingSpeed);
        addSetting(equipSpeed);
        addSetting(swingHeight);
        addSetting(swingProgress);
        addSetting(mainHandOnly);
        addSetting(ignoreMine);
        INSTANCE = this;
    }

    public static float getSwingMultiplier() {
        if (INSTANCE == null || !INSTANCE.isEnabled()) return 1.0f;
        return switch (INSTANCE.mode.get()) {
            case "Slow" -> (float) Math.max(0.05, INSTANCE.swingSpeed.get() * 0.55);
            case "Smooth" -> (float) Math.max(0.08, INSTANCE.swingSpeed.get() * 0.70);
            case "Static" -> 0.0f;
            case "Fast" -> (float) Math.min(2.0, INSTANCE.swingSpeed.get() * 1.35);
            default -> (float) INSTANCE.swingSpeed.get();
        };
    }

    public static float getEquipMultiplier() {
        if (INSTANCE == null || !INSTANCE.isEnabled()) return 1.0f;
        if ("Static".equals(INSTANCE.mode.get())) return 0.0f;
        return (float) INSTANCE.equipSpeed.get();
    }

    public static float getSwingHeightOffset() {
        if (INSTANCE == null || !INSTANCE.isEnabled()) return 0f;
        return (float) INSTANCE.swingHeight.get();
    }

    public static float getProgressScale() {
        if (INSTANCE == null || !INSTANCE.isEnabled()) return 1f;
        return (float) INSTANCE.swingProgress.get();
    }

    public static boolean mainHandOnly() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.mainHandOnly.get();
    }

    public static boolean ignoreMining() {
        return INSTANCE != null && INSTANCE.isEnabled() && INSTANCE.ignoreMine.get();
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        setTag(mode.get());
    }

    @Override
    public void onDisable() {
        setTag(null);
    }

    @Override
    public void onTick() {
        if (isEnabled()) setTag(mode.get());
    }

    private void setTag(String t) {
        try {
            var f = Module.class.getDeclaredField("tag");
            f.setAccessible(true);
            f.set(this, t);
        } catch (Throwable ignored) {}
    }
}
