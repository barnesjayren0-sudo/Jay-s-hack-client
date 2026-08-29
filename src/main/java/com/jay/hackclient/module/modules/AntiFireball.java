package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.Hand;

/** Punch nearby fireballs away. */
public class AntiFireball extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Punch range", 4.5, 2.0, 6.0, 0.1);

    public AntiFireball() {
        super("AntiFireball", "Punch fireballs", Category.COMBAT);
        addSetting(range);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof FireballEntity fb)) continue;
            if (mc.player.distanceTo(fb) > range.get()) continue;
            mc.interactionManager.attackEntity(mc.player, fb);
            mc.player.swingHand(Hand.MAIN_HAND);
            return;
        }
    }
}
