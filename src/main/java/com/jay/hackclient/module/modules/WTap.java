package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

/** WTap — sprint reset on hit (W-tap). Public setTag, restores sprint on disable. */
public class WTap extends Module {
    public final NumberSetting holdMs = new NumberSetting("Hold", "Release window ms", 85, 40, 160, 5);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Sprint command packets", true);
    private long resetUntil;
    private boolean restoring;
    private int lastSwing = -1;

    public WTap() { super("WTap", "Sprint reset on hit (W-tap)", Category.COMBAT); addSetting(holdMs); addSetting(realPackets); }

    @Override public void onDisable() {
        if (restoring && mc.player != null && mc.options != null) {
            mc.options.forwardKey.setPressed(true);
            if (realPackets.get() && mc.options.forwardKey.isPressed()) RealPackets.startSprint();
        }
        restoring = false;
        resetUntil = 0;
        lastSwing = -1;
        setTag(null);
    }

    @Override    public void onTick() {
        try { if (!com.jay.hackclient.util.CombatManager.canCombatModulesRun()) return; } catch (Throwable ignored) {}
        if (mc.player == null || mc.options == null) return;
        long now = System.currentTimeMillis();
        if (now < resetUntil) {
            mc.options.forwardKey.setPressed(false);
            mc.player.setSprinting(false);
            if (realPackets.get()) RealPackets.stopSprint();
            restoring = true;
            setTag("reset");
            return;
        }
        if (restoring) {
            restoring = false;
            if (mc.options.forwardKey.isPressed() && realPackets.get()) RealPackets.startSprint();
        }
        setTag(null);
        int swing = mc.player.handSwingTicks;
        boolean newSwing = swing > 0 && swing != lastSwing && mc.player.handSwinging;
        lastSwing = swing;
        if (!newSwing) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof PlayerEntity p) || p == mc.player) return;
        if (!mc.options.forwardKey.isPressed()) return;
        // Humanized hold window — jittered per tap so resets never look metronome-regular
        long hold = Humanizer.delay((int) holdMs.get(), 18, 40, 220);
        resetUntil = now + hold;
        mc.options.forwardKey.setPressed(false);
        mc.player.setSprinting(false);
        if (realPackets.get()) RealPackets.stopSprint();
        restoring = true;
    }
}
