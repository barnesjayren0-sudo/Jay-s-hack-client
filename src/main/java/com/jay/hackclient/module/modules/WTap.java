package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Sprint-reset on hit — brief W release. */
public class WTap extends Module {

    public final NumberSetting holdMs = new NumberSetting("Hold", "Release window ms", 85, 40, 160, 5);

    private long resetUntil = 0;
    private boolean wasForward;

    public WTap() {
        super("WTap", "Sprint reset on hit", Category.COMBAT);
        addSetting(holdMs);
    }

    @Override
    public void onDisable() {
        if (wasForward && mc.options != null) {
            mc.options.forwardKey.setPressed(true);
        }
        wasForward = false;
        resetUntil = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;

        long now = System.currentTimeMillis();
        if (now < resetUntil) {
            mc.options.forwardKey.setPressed(false);
            return;
        }
        if (wasForward) {
            mc.options.forwardKey.setPressed(true);
            wasForward = false;
        }

        if (mc.player.getAttackCooldownProgress(0.0f) > 0.92f) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        Entity e = ehr.getEntity();
        if (!(e instanceof PlayerEntity) || e == mc.player) return;
        if (mc.player.handSwingTicks != 1 && mc.player.handSwinging) {
            // just swung
        }
        if (mc.player.hurtTime > 0) return;

        // Trigger only right after our attack swing
        if (mc.player.getLastAttackedTicks() <= 2 || mc.player.handSwingProgress > 0.7f) {
            if (mc.options.forwardKey.isPressed()) {
                wasForward = true;
                int ms = (int) holdMs.get();
                resetUntil = now + Math.max(40, Humanizer.tapResetMs() / 2 + ms / 2);
                mc.options.forwardKey.setPressed(false);
            }
        }
    }
}
