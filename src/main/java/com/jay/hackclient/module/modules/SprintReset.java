package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

public class SprintReset extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Reset variant", "W-Tap", "W-Tap", "S-Tap");
    public final NumberSetting holdMs = new NumberSetting("Hold Ms", "Key hold duration", 80, 30, 160, 5);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Sprint command packets", true);
    private long until; private boolean active; private int lastSwing = -1;
    public SprintReset() { super("SprintReset", "W-Tap / S-Tap sprint reset", Category.COMBAT); addSetting(mode); addSetting(holdMs); addSetting(realPackets); }
    @Override public void onDisable() { cleanup(); }
    @Override public void onTick() {
        if (mc.player == null || mc.options == null) return;
        long now = System.currentTimeMillis();
        if (now < until) { applyHold(); return; }
        if (active) { cleanup(); if (mc.options.forwardKey.isPressed() && realPackets.get()) RealPackets.startSprint(); }
        int swing = mc.player.handSwingTicks;
        boolean newSwing = swing > 0 && swing != lastSwing && mc.player.handSwinging;
        lastSwing = swing; if (!newSwing) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof PlayerEntity)) return;
        until = now + (long) holdMs.get(); active = true; applyHold();
    }
    private void applyHold() {
        mc.player.setSprinting(false);
        if (realPackets.get()) RealPackets.stopSprint();
        if ("S-Tap".equals(mode.get())) { mc.options.backKey.setPressed(true); mc.options.forwardKey.setPressed(false); }
        else mc.options.forwardKey.setPressed(false);
    }
    private void cleanup() {
        if (mc.options != null && "S-Tap".equals(mode.get())) mc.options.backKey.setPressed(false);
        active = false; until = 0;
    }
}
