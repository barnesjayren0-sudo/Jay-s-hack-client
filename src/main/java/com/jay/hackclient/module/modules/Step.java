package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

/** Higher step height via STEP_HEIGHT attribute (1.21+). */
public class Step extends Module {

    public final NumberSetting height = new NumberSetting("Height", "Step height", 1.0, 0.6, 2.5, 0.1);
    private double oldStep = 0.6;

    public Step() {
        super("Step", "Walk up blocks without jumping", Category.ANARCHY);
        addSetting(height);
    }

    private EntityAttributeInstance attr() {
        if (mc.player == null) return null;
        try {
            return mc.player.getAttributeInstance(EntityAttributes.STEP_HEIGHT);
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    public void onEnable() {
        EntityAttributeInstance a = attr();
        if (a != null) oldStep = a.getBaseValue();
    }

    @Override
    public void onDisable() {
        EntityAttributeInstance a = attr();
        if (a != null) a.setBaseValue(oldStep);
    }

    @Override
    public void onTick() {
        EntityAttributeInstance a = attr();
        if (a != null) a.setBaseValue(height.get());
    }
}
