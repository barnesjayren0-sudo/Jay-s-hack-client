package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

/** Sprint-reset on hit — brief forward release. */
public class WTap extends Module {

    public final NumberSetting holdMs = new NumberSetting("Hold", "Release window ms", 85, 40, 160, 5);

    private long resetUntil = 0;
    private boolean restoring;
    private int lastSwing = -1;

    public WTap() {
        super("WTap", "Sprint reset on hit", Category.COMBAT);
        addSetting(holdMs);
    }

    @Override
    public void onDisable() {
        if (restoring && mc.options != null) {
            // leave key state to player
        }
        restoring = false;
        resetUntil = 0;
        lastSwing = -1;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;

        long now = System.currentTimeMillis();
        if (now < resetUntil) {
            mc.options.forwardKey.setPressed(false);
            restoring = true;
            return;
        }
        if (restoring) {
            restoring = false;
            // don't force forward back on — avoids sticky W
        }

        int swing = mc.player.handSwingTicks;
        boolean newSwing = swing > 0 && swing != lastSwing && mc.player.handSwinging;
        lastSwing = swing;
        if (!newSwing) return;

        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof PlayerEntity p) || p == mc.player) return;
        if (!mc.options.forwardKey.isPressed()) return;

        int ms = (int) holdMs.get();
        resetUntil = now + Math.max(40, (Humanizer.tapResetMs() + ms) / 2);
        mc.options.forwardKey.setPressed(false);
        restoring = true;
    }
}
