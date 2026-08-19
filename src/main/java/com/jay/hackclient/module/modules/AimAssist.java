package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.SilentRotations;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * Classic soft AimAssist (old style — continuous smooth pull) + improvements:
 * - FOV limit so it only helps when you're already near the target
 * - Distance-scaled strength (stronger when closer)
 * - Humanized jitter / skip ticks
 * - Multiplier based on whether you're pressing attack
 * Key: J
 */
public class AimAssist extends Module {

    private final double range = 4.8;
    private final float fov = 65f;
    private final float baseSmooth = 0.32f; // old-style visible assist strength

    public AimAssist() {
        super("AimAssist", "Classic soft aim (improved) — key J", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick(8)) return;
        if (Mobile.shouldThrottle()) return;

        PlayerEntity target = findTarget();
        if (target == null) return;

        // Only assist inside FOV — feels like aim help, not lock
        if (!SilentRotations.inFov(target, fov)) return;

        double dist = mc.player.distanceTo(target);
        // Closer = slightly stronger (old assist felt better up close)
        float distFactor = (float) MathHelper.clamp(1.15 - (dist / range) * 0.5, 0.55, 1.15);

        float smooth = baseSmooth * distFactor;
        if (mc.options.attackKey.isPressed()) {
            smooth *= 1.25f; // help more while clicking
        } else {
            smooth *= 0.75f; // softer when just looking
        }
        smooth = Humanizer.aimSmooth(smooth);

        // Classic continuous pull (the "old better" feel)
        RotationUtil.lookAt(target, smooth);
    }

    private PlayerEntity findTarget() {
        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (AntiBot.isBot(p)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;

            double d = mc.player.distanceTo(p);
            if (d > range) continue;
            if (!SilentRotations.inFov(p, fov + 10f)) continue;

            // Prefer closest + most centered in FOV
            float[] ang = SilentRotations.anglesTo(p);
            if (ang == null) continue;
            float dyaw = Math.abs(MathHelper.wrapDegrees(ang[0] - mc.player.getYaw()));
            double score = d + dyaw * 0.04;
            if (score < bestScore) {
                bestScore = score;
                best = p;
            }
        }
        return best;
    }
}
