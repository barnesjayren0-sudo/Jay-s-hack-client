package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireballEntity;

public class AntiFireball extends Module {
    public final NumberSetting range = new NumberSetting("Range", "Hit range", 4.0, 2.0, 6.0, 0.1);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Attack packets", true);
    public AntiFireball() { super("AntiFireball", "Punch incoming fireballs", Category.COMBAT); addSetting(range); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null) return;
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof FireballEntity fb)) continue;
            if (mc.player.distanceTo(fb) > range.get()) continue;
            if (realPackets.get()) RealPackets.attackEntity(fb);
            else if (mc.interactionManager != null) { mc.interactionManager.attackEntity(mc.player, fb); mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND); }
            return;
        }
    }
}
