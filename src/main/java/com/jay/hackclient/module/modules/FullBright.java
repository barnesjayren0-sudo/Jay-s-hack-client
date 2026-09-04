package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Night vision style brightness. */
public class FullBright extends Module {

    public final NumberSetting gamma = new NumberSetting("Gamma", "Fallback gamma", 16.0, 1.0, 16.0, 0.5);
    private Double savedGamma = null;

    public FullBright() {
        super("FullBright", "See in the dark", Category.RENDER);
        addSetting(gamma);
    }

    @Override
    public void onEnable() {
        try {
            if (mc.options != null) {
                savedGamma = mc.options.getGamma().getValue();
                mc.options.getGamma().setValue(Math.max(gamma.get(), 12.0));
            }
        } catch (Throwable ignored) {}
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
        try {
            if (savedGamma != null && mc.options != null) {
                mc.options.getGamma().setValue(savedGamma);
            }
        } catch (Throwable ignored) {}
        savedGamma = null;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        StatusEffectInstance nv = new StatusEffectInstance(StatusEffects.NIGHT_VISION, 300, 0, false, false, false);
        mc.player.addStatusEffect(nv);
    }
}
