package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

public class KillAura extends Module {

    private long lastAttack = 0;
    private int nextDelay = 480;
    private final double range = 4.15;

    public KillAura() {
        super("KillAura", "Legit-timed sword aura", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;

        // Smooth look (harder to flag than instant snap)
        RotationUtil.lookAt(target, 0.55f);

        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastAttack = now;
        nextDelay = MathUtil.randomDelay(420, 620); // humanized CPS
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
