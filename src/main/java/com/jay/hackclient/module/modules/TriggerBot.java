package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
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

    public final BoolSetting playersOnly = new BoolSetting("Players", "Players only", true);
    public final BoolSetting weaponOnly = new BoolSetting("Weapon", "Sword/axe only", true);
    public final NumberSetting minCooldown = new NumberSetting("Cooldown", "Min attack progress", 0.88, 0.5, 1.0, 0.01);

    private long lastAttack = 0;
    private int nextDelay = 550;

    public TriggerBot() {
        super("TriggerBot", "Hit on crosshair — [T]", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_T);
        addSetting(playersOnly);
        addSetting(weaponOnly);
        addSetting(minCooldown);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.combatDelay();
        lastAttack = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (weaponOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null || mc.player.isUsingItem()) return;
        if (Humanizer.shouldSkipTick()) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;

        EntityHitResult hit = (EntityHitResult) mc.crosshairTarget;
        Entity entity = hit.getEntity();
        if (!(entity instanceof PlayerEntity player)) {
            if (playersOnly.get()) return;
            // non-player entity hit path
            long now = System.currentTimeMillis();
            if (now - lastAttack < nextDelay) return;
            if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < minCooldown.getFloat()) return;
            mc.interactionManager.attackEntity(mc.player, entity);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        if (player == mc.player || !player.isAlive()) return;
        try { if (AntiBot.isBot(player)) return; } catch (Throwable ignored) {}
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(player.getName().getString())) return;

        double maxDist = Reach.isActive() ? Reach.getReach() + 0.15 : 3.15;
        try { maxDist += Hitboxes.getExpand(); } catch (Throwable ignored) {}
        if (mc.player.distanceTo(player) > maxDist) return;

        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < minCooldown.getFloat()) return;
        try {
            if (ClientSettings.critTiming && !CritAssist.canAttackNow(mc.player)) return;
            if (!ComboHit.shouldAttack(mc.player, player)) return;
        } catch (Throwable ignored) {}

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;

        if (Humanizer.shouldMiss()) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        try { ReachHUD.recordHit(mc.player.distanceTo(player)); } catch (Throwable ignored) {}
        mc.interactionManager.attackEntity(mc.player, player);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }
}
