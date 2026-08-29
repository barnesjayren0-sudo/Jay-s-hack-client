package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Soft no-slow while using items. */
public class NoSlow extends Module {

    public final BoolSetting items = new BoolSetting("Items", "While using items", true);
    public final NumberSetting factor = new NumberSetting("Factor", "Keep speed factor", 0.85, 0.5, 1.0, 0.05);

    public NoSlow() {
        super("NoSlow", "Less slowdown when using items", Category.MOVEMENT);
        addSetting(items);
        addSetting(factor);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (items.get() && mc.player.isUsingItem()) {
            Vec3d v = mc.player.getVelocity();
            double f = factor.get();
            // Boost horizontal slightly toward normal walk speed
            mc.player.setVelocity(v.x * (1.0 + (1.0 - f) * 0.4), v.y, v.z * (1.0 + (1.0 - f) * 0.4));
        }
    }
}
