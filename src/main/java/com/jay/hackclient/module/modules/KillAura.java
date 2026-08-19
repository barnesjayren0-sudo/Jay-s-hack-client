package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class KillAura extends Module {

    private long lastAttack = 0;
    private int nextDelay = 560;
    private final double range = 3.5;

    public KillAura() {
        super("KillAura", "Aura with soft track between hits", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick(5)) return;
        if (Mobile.shouldThrottle()) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) {
            // soft track like classic assist between hits
            if (Humanizer.chance(65)) RotationUtil.lookAt(target, 0.2f);
            return;
        }

        float cd = mc.player.getAttackCooldownProgress(0.5f);
        if (cd < 0.8f && Humanizer.chance(60)) return;

        RotationUtil.lookAt(target, 0.5f);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double closest = range;

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            if (AntiBot.isBot(player)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(player.getName().getString())) continue;

            double dist = mc.player.distanceTo(player);
            if (dist <= closest) {
                closest = dist;
                best = player;
            }
        }
        return best;
    }
}
