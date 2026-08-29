package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Soft blink pulse — short hold after combat. */
public class SoftBlink extends Module {

    public final NumberSetting holdMs = new NumberSetting("HoldMs", "Hold duration", 80, 40, 200, 5);
    public final NumberSetting cooldown = new NumberSetting("Cooldown", "Ms between pulses", 400, 200, 1200, 50);

    private long pulseUntil;
    private long lastPulse;
    private Vec3d holdPos;

    public SoftBlink() {
        super("SoftBlink", "Short soft lag pulse in combat", Category.COMBAT);
        addSetting(holdMs);
        addSetting(cooldown);
    }

    @Override
    public void onDisable() {
        holdPos = null;
        pulseUntil = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();

        if (now < pulseUntil && holdPos != null) {
            mc.player.setVelocity(mc.player.getVelocity().multiply(0.35, 1.0, 0.35));
            double dx = holdPos.x - mc.player.getX();
            double dz = holdPos.z - mc.player.getZ();
            if (dx * dx + dz * dz > 0.04) {
                mc.player.setPosition(
                        holdPos.x * 0.3 + mc.player.getX() * 0.7,
                        mc.player.getY(),
                        holdPos.z * 0.3 + mc.player.getZ() * 0.7
                );
            }
            return;
        }

        boolean combat = mc.player.hurtTime > 0 || mc.player.getAttackCooldownProgress(0.5f) < 0.3f;
        if (!combat) return;
        if (now - lastPulse < cooldown.getInt()) return;

        holdPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        pulseUntil = now + holdMs.getInt();
        lastPulse = now;
    }
}
