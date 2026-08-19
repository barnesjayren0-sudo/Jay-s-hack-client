package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class TriggerBot extends Module {

    private long lastAttack = 0;
    private int nextDelay = 500;

    public TriggerBot() {
        super("TriggerBot", "Attack when on target", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_T);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null) return;
        if (mc.player.isUsingItem()) return; // don't punch while blocking/eating
        if (Humanizer.shouldSkipTick(3)) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity player)) return;
        if (player == mc.player || !player.isAlive()) return;
        if (AntiBot.isBot(player)) return;
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(player.getName().getString())) return;

        float cd = mc.player.getAttackCooldownProgress(0.5f);
        if (cd < 0.9f) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;

        mc.interactionManager.attackEntity(mc.player, player);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }
}
