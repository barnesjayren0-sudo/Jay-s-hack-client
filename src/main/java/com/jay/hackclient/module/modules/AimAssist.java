package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.hit.HitResult;

/** Soft aim — only pulls crosshair toward target, does not auto-click. */
public class AimAssist extends Module {

    private final double range = 4.5;
    private final float strength = 0.28f; // lower = more legit

    public AimAssist() {
        super("AimAssist", "Smoothly aims toward nearest enemy", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
        if (mc.currentScreen != null) return;

        // Don't fight the player if they're already looking at something else closely
        PlayerEntity target = findTarget();
        if (target == null) return;

        RotationUtil.lookAt(target, strength);
    }

    private PlayerEntity findTarget() {
        PlayerEntity best = null;
        double closest = range;

        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof PlayerEntity p)) continue;
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;

            double d = mc.player.distanceTo(p);
            if (d < closest) {
                closest = d;
                best = p;
            }
        }
        return best;
    }
}
