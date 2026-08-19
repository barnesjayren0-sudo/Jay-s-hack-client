package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Detects blocking players and swaps to axe, hits, then can restore sword.
 */
public class ShieldBreak extends Module {

    private long lastSwap = 0;
    private int savedSlot = -1;
    private long restoreAt = 0;

    public ShieldBreak() {
        super("ShieldBreak", "Axe-swap vs shields", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_V);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        long now = System.currentTimeMillis();

        // Delayed sword restore after axe hit window
        if (savedSlot >= 0 && now >= restoreAt && restoreAt > 0) {
            if (ItemUtil.isAxe(mc.player.getMainHandStack())) {
                mc.player.getInventory().setSelectedSlot(savedSlot);
            }
            savedSlot = -1;
            restoreAt = 0;
        }

        PlayerEntity target = getTarget();
        if (target == null) return;
        if (AntiBot.isBot(target)) return;

        boolean blocking = target.isBlocking();
        if (!blocking) return;

        if (now - lastSwap < Humanizer.swapDelay()) return;

        int axe = findAxe();
        if (axe < 0) return;

        if (savedSlot < 0) {
            savedSlot = mc.player.getInventory().getSelectedSlot();
        }

        if (mc.player.getInventory().getSelectedSlot() != axe) {
            mc.player.getInventory().setSelectedSlot(axe);
            lastSwap = now;
        }

        // If looking at them and cooldown ready, hit to disable shield
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ehr = (EntityHitResult) mc.crosshairTarget;
            if (ehr.getEntity() == target) {
                float cd = mc.player.getAttackCooldownProgress(0.5f);
                if (cd >= 0.9f) {
                    mc.interactionManager.attackEntity(mc.player, target);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    restoreAt = now + Humanizer.delay(120, 30, 80, 220);
                    lastSwap = now;
                }
            }
        }
    }

    private PlayerEntity getTarget() {
        // Prefer crosshair
        if (mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity p
                && p != mc.player) {
            return p;
        }
        // Fallback nearest blocking player in 4 blocks
        PlayerEntity best = null;
        double bestD = 4.0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (!p.isBlocking()) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    private int findAxe() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (ItemUtil.isAxe(s)) return i;
            if (s.isOf(Items.NETHERITE_AXE) || s.isOf(Items.DIAMOND_AXE)
                    || s.isOf(Items.IRON_AXE) || s.isOf(Items.STONE_AXE)
                    || s.isOf(Items.GOLDEN_AXE) || s.isOf(Items.WOODEN_AXE)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot >= 0) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        restoreAt = 0;
    }
}
