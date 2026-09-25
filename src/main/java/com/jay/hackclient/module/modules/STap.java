package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

public class STap extends Module {
    public final NumberSetting holdMs = new NumberSetting("Hold", "S hold ms", 70, 30, 140, 5);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Sprint command packets", true);
    private long until; private boolean holding; private int lastSwing = -1;
    public STap() { super("STap", "S-tap sprint reset on hit", Category.COMBAT); addSetting(holdMs); addSetting(realPackets); }
    @Override public void onDisable() {
        if (holding && mc.options != null) { mc.options.backKey.setPressed(false); if (realPackets.get()) RealPackets.startSprint(); }
        holding = false; until = 0;
    }
    @Override public void onTick() {
        if (mc.player == null || mc.options == null) return;
        long now = System.currentTimeMillis();
        if (now < until) {
            mc.options.backKey.setPressed(true); mc.player.setSprinting(false);
            if (realPackets.get()) RealPackets.stopSprint(); holding = true; return;
        }
        if (holding) {
            mc.options.backKey.setPressed(false); holding = false;
            if (mc.options.forwardKey.isPressed() && realPackets.get()) RealPackets.startSprint();
        }
        int swing = mc.player.handSwingTicks;
        boolean newSwing = swing > 0 && swing != lastSwing && mc.player.handSwinging;
        lastSwing = swing; if (!newSwing) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof PlayerEntity)) return;
        until = now + (long) holdMs.get();
        mc.options.backKey.setPressed(true); mc.player.setSprinting(false);
        if (realPackets.get()) RealPackets.stopSprint(); holding = true;
    }
}
