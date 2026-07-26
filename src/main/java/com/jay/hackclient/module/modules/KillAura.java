package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class KillAura extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();
    private long lastAttack = 0;

    // Settings (can be expanded later)
    private final double range = 4.2;
    private final int delayMs = 480; // ~2.08 CPS - good for sword

    public KillAura() {
        super("KillAura", "Sword-only KillAura with smart targeting", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        // Only attack while holding a sword
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        if (System.currentTimeMillis() - lastAttack >= delayMs) {
            // Face the target (simple look)
            lookAt(target);

            // Attack
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);

            lastAttack = System.currentTimeMillis();
        }
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double closest = range;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof PlayerEntity player)) continue;
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;

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
        Vec3d targetPos = target.getPos().add(0, target.getHeight() * 0.85, 0);

        double dx = targetPos.x - eyes.x;
        double dy = targetPos.y - eyes.y;
        double dz = targetPos.z - eyes.z;

        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
