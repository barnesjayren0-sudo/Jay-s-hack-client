package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

/** Light 1.8-style block timing: right-click block briefly after a hit. */
public class SwordBlock extends Module {

    public final NumberSetting blockMs = new NumberSetting("BlockMs", "Block hold ms", 80, 40, 200, 5);

    private long blockUntil;

    public SwordBlock() {
        super("SwordBlock", "Short sword block after hit", Category.COMBAT);
        addSetting(blockMs);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (!ItemUtil.isSword(mc.player.getMainHandStack())) return;

        long now = System.currentTimeMillis();

        // After attacking an entity, hold use for a short window
        if (mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity
                && mc.player.getAttackCooldownProgress(0.5f) < 0.2f) {
            blockUntil = now + blockMs.getInt();
        }

        if (now < blockUntil) {
            // Client-side use key press simulation
            mc.options.useKey.setPressed(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            // don't leave use stuck — only release if we were controlling
            blockUntil = 0;
        }
    }
}
