package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class ShieldBreak extends Module {

    private long lastSwap = 0;
    private int savedSlot = -1;
    private long restoreAt = 0;

    public ShieldBreak() {
        super("ShieldBreak", "Axe-swap vs shields — key V", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_V);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        long now = System.currentTimeMillis();

        if (savedSlot >= 0 && now >= restoreAt && restoreAt > 0) {
            if (ItemUtil.isAxe(mc.player.getMainHandStack())) {
                mc.player.getInventory().setSelectedSlot(savedSlot);
            }
            SlotLock.release("ShieldBreak");
            savedSlot = -1;
            restoreAt = 0;
        }

        PlayerEntity target = getTarget();
        if (target == null || AntiBot.isBot(target) || !target.isBlocking()) return;
        if (now - lastSwap < Humanizer.swapDelay()) return;

        int axe = findAxe();
        if (axe < 0) return;
        if (!SlotLock.tryAcquire("ShieldBreak", 400)) return;

        if (savedSlot < 0) savedSlot = mc.player.getInventory().getSelectedSlot();
        if (mc.player.getInventory().getSelectedSlot() != axe) {
            mc.player.getInventory().setSelectedSlot(axe);
            lastSwap = now;
        }

        if (mc.crosshairTarget instanceof EntityHitResult ehr && ehr.getEntity() == target) {
            if (mc.player.getAttackCooldownProgress(0.5f) >= 0.9f) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
                restoreAt = now + Humanizer.delay(120, 30, 80, 220);
                lastSwap = now;
            }
        }
    }

    private PlayerEntity getTarget() {
        if (mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity p && p != mc.player) return p;
        PlayerEntity best = null;
        double bestD = 4.0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || !p.isBlocking()) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestD) { bestD = d; best = p; }
        }
        return best;
    }

    private int findAxe() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (ItemUtil.isAxe(s) || s.isOf(Items.NETHERITE_AXE) || s.isOf(Items.DIAMOND_AXE)
                    || s.isOf(Items.IRON_AXE)) return i;
        }
        return -1;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot >= 0) mc.player.getInventory().setSelectedSlot(savedSlot);
        SlotLock.release("ShieldBreak");
        savedSlot = -1;
    }
}
