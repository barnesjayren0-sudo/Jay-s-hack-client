package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Swaps to axe when target is blocking with shield (kit PvP). */
public class ShieldBreak extends Module {

    private long lastSwap = 0;
    private int savedSlot = -1;

    public ShieldBreak() {
        super("ShieldBreak", "Axe swap when enemy shields", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;

        PlayerEntity target = getCrosshairPlayer();
        if (target == null || !target.isBlocking()) {
            restoreSword();
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSwap < MathUtil.randomDelay(40, 100)) return;

        int axe = findAxe();
        if (axe == -1) return;

        if (savedSlot < 0) {
            savedSlot = mc.player.getInventory().getSelectedSlot();
        }
        if (mc.player.getInventory().getSelectedSlot() != axe) {
            mc.player.getInventory().setSelectedSlot(axe);
            lastSwap = now;
        }
    }

    private void restoreSword() {
        if (savedSlot < 0 || mc.player == null) return;
        // only restore if currently holding axe
        if (ItemUtil.isAxe(mc.player.getMainHandStack())) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
    }

    private PlayerEntity getCrosshairPlayer() {
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return null;
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return null;
        if (ehr.getEntity() instanceof PlayerEntity p && p != mc.player) return p;
        return null;
    }

    private int findAxe() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (ItemUtil.isAxe(s) || s.isOf(Items.NETHERITE_AXE) || s.isOf(Items.DIAMOND_AXE)
                    || s.isOf(Items.IRON_AXE)) {
                return i;
            }
        }
        return -1;
    }
}
