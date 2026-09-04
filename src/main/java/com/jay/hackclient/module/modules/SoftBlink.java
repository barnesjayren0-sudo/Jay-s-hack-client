package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;

/**
 * Soft blink — velocity damp only (no setPosition — was causing desync).
 */
public class SoftBlink extends Module {

    public final NumberSetting holdMs = new NumberSetting("HoldMs", "Hold duration", 70, 40, 180, 5);
    public final NumberSetting cooldown = new NumberSetting("Cooldown", "Ms between pulses", 450, 200, 1200, 50);
    public final NumberSetting damp = new NumberSetting("Damp", "XZ velocity scale", 0.40, 0.15, 0.7, 0.05);

    private long pulseUntil;
    private long lastPulse;

    public SoftBlink() {
        super("SoftBlink", "Short soft lag pulse in combat", Category.COMBAT);
        addSetting(holdMs);
        addSetting(cooldown);
        addSetting(damp);
    }

    @Override
    public void onDisable() {
        pulseUntil = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();

        if (now < pulseUntil) {
            double d = damp.get();
            var v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * d, v.y, v.z * d);
            return;
        }

        boolean combat = mc.player.hurtTime > 0
                || (mc.player.getAttackCooldownProgress(0.5f) < 0.35f && mc.player.handSwinging);
        if (!combat) return;
        if (now - lastPulse < cooldown.getInt()) return;

        pulseUntil = now + holdMs.getInt();
        lastPulse = now;
    }
}
