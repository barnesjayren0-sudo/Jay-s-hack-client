package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

/** SoftBlink — Damp / GroundSpoof / Combo pulse. Real on-ground packets. */
public class SoftBlink extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Pulse style", "Damp", "Damp", "GroundSpoof", "Combo");
    public final NumberSetting holdMs = new NumberSetting("Hold Ms", "Pulse duration", 70, 30, 200, 5);
    public final NumberSetting cooldown = new NumberSetting("Cooldown", "Ms between pulses", 400, 150, 1200, 50);
    public final NumberSetting damp = new NumberSetting("Damp", "XZ velocity scale", 0.35, 0.1, 0.8, 0.05);
    public final BoolSetting onHurt = new BoolSetting("On Hurt", "Pulse when hurt", true);
    public final BoolSetting onHit = new BoolSetting("On Hit", "Pulse after you hit", true);

    private long pulseUntil, lastPulse;

    public SoftBlink() {
        super("SoftBlink", "Short soft lag pulse in combat", Category.COMBAT);
        addSetting(mode); addSetting(holdMs); addSetting(cooldown); addSetting(damp);
        addSetting(onHurt); addSetting(onHit);
    }

    @Override public void onDisable() { pulseUntil = 0; setTag(null); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now < pulseUntil) { applyPulse(); setTag("pulse"); return; }
        boolean combat = false;
        if (onHurt.get() && mc.player.hurtTime > 0) combat = true;
        if (onHit.get() && mc.player.getAttackCooldownProgress(0.5f) < 0.4f && mc.player.handSwinging) combat = true;
        if (!combat) { setTag(null); return; }
        if (now - lastPulse < cooldown.get()) return;
        pulseUntil = now + (long) holdMs.get();
        lastPulse = now;
    }

    private void applyPulse() {
        String m = mode.get();
        if ("Damp".equals(m) || "Combo".equals(m)) {
            double d = damp.get();
            var v = mc.player.getVelocity();
            mc.player.setVelocity(v.x * d, v.y, v.z * d);
        }
        if ("GroundSpoof".equals(m) || "Combo".equals(m)) {
            RealPackets.sendOnGround(!mc.player.isOnGround());
        }
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
