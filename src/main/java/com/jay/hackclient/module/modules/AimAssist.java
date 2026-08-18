package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class AimAssist extends Module {

    private final double range = 4.2;

    public AimAssist() {
        super("AimAssist", "Soft aim with jitter", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null) return;
        if (Humanizer.shouldSkipTick(10)) return;

        PlayerEntity target = findTarget();
        if (target == null) return;

        // weak pull — looks more like assist than lock
        RotationUtil.lookAt(target, Humanizer.aimSmooth(0.22f));
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
