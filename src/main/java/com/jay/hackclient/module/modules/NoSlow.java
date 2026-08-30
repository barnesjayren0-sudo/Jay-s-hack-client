package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;

/** Soft no-slow — default only while blocking with shield. */
public class NoSlow extends Module {

    public final BoolSetting shieldOnly = new BoolSetting("ShieldOnly", "Only while blocking", true);
    public final BoolSetting items = new BoolSetting("Items", "Also while eating/using", false);
    public final NumberSetting factor = new NumberSetting("Factor", "Keep speed factor", 0.88, 0.5, 1.0, 0.05);

    public NoSlow() {
        super("NoSlow", "Less slowdown when blocking", Category.MOVEMENT);
        addSetting(shieldOnly);
        addSetting(items);
        addSetting(factor);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        boolean blocking = mc.player.isBlocking()
                || (mc.player.isUsingItem()
                && (mc.player.getActiveItem().isOf(Items.SHIELD)
                || mc.player.getOffHandStack().isOf(Items.SHIELD)
                || mc.player.getMainHandStack().isOf(Items.SHIELD)));

        boolean using = mc.player.isUsingItem();

        if (shieldOnly.get() && !blocking) return;
        if (!shieldOnly.get() && items.get() && !using && !blocking) return;
        if (!shieldOnly.get() && !items.get() && !blocking) return;

        if (!blocking && !(items.get() && using)) return;

        Vec3d v = mc.player.getVelocity();
        double f = factor.get();
        // Soft boost — not full noslow
        double mult = 1.0 + (1.0 - f) * 0.35;
        mc.player.setVelocity(v.x * mult, v.y, v.z * mult);
    }
}
