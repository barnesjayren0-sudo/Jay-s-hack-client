package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class KillAura extends Module {

    private long lastAttack = 0;
    private int nextDelay = 550;
    private final double range = 3.6; // quieter default than 4.2

    public KillAura() {
        super("KillAura", "Sword/axe aura (legit range + jitter)", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) {
            RotationUtil.lookAt(target, 0.18f); // very soft track
            return;
        }

        RotationUtil.lookAt(target, 0.45f);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastAttack = now;
        // wider humanized window
        nextDelay = MathUtil.randomDelay(480, 720);
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double closest = range;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
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
