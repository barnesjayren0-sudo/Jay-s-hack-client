package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class KillAura extends Module {

    private long lastAttack = 0;
    private final double range = 4.2;
    private final int delayMs = 500;

    public KillAura() {
        super("KillAura", "Sword-only KillAura with targeting", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < delayMs) return;

        lookAt(target);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastAttack = now;
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double closest = range;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            if (JayHackClient.friendManager != null && JayHackClient.friendManager.isFriend(player.getName().getString())) continue;

            double dist = mc.player.distanceTo(player);
            if (dist <= closest) {
                closest = dist;
                best = player;
            }
        }
        return best;
    }

    private void lookAt(LivingEntity target) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.85, 0);
        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        mc.player.setYaw(yaw);
        mc.player.setPitch(MathHelper.clamp(pitch, -90f, 90f));
    }
}
