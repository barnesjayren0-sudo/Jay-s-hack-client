package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

/** S-tap sprint reset alternative. */
public class STap extends Module {

    public final NumberSetting holdMs = new NumberSetting("Hold", "S hold ms", 70, 30, 140, 5);

    private long until;
    private boolean holding;

    public STap() {
        super("STap", "Back-tap sprint reset", Category.COMBAT);
        addSetting(holdMs);
    }

    @Override
    public void onDisable() {
        if (holding && mc.options != null) {
            mc.options.backKey.setPressed(false);
        }
        holding = false;
        until = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        long now = System.currentTimeMillis();
        if (now < until) {
            mc.options.backKey.setPressed(true);
            holding = true;
            return;
        }
        if (holding) {
            mc.options.backKey.setPressed(false);
            holding = false;
        }

        if (!mc.player.handSwinging || mc.player.handSwingTicks > 3) return;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return;
        if (!(ehr.getEntity() instanceof PlayerEntity)) return;
        if (!mc.options.forwardKey.isPressed()) return;

        until = now + Math.max(30, (int) holdMs.get());
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(true);
        holding = true;
    }
}
