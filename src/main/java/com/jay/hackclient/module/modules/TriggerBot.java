package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
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
    private int nextDelay = 550;

    public TriggerBot() {
        super("TriggerBot", "Crosshair hits only", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_T);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.combatDelay();
        lastAttack = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null || mc.player.isUsingItem()) return;
        if (Humanizer.shouldSkipTick()) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity player)) return;
        if (player == mc.player || !player.isAlive()) return;
        if (AntiBot.isBot(player)) return;
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(player.getName().getString())) return;

        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.9f) return;
        if (ClientSettings.critTiming && !CritAssist.canAttackNow(mc.player)) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;

        if (Humanizer.shouldMiss()) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        mc.interactionManager.attackEntity(mc.player, player);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }
}
