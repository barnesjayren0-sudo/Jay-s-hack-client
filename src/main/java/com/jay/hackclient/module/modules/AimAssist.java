package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * AimAssist v1.10 — stronger classic soft-aim that still feels controllable.
 * Key: J
 */
public class AimAssist extends Module {

    private final double range = 5.0;
    private final float maxFov = 90f;

    public AimAssist() {
        super("AimAssist", "Smooth aim assist — key J", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())
                && !ItemUtil.isSwordOrAxe(mc.player.getOffHandStack())) {
            // allow main hand weapons only primarily
            if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        }
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick(4)) return;
        if (Mobile.shouldThrottle()) return;

        PlayerEntity target = findTarget();
        if (target == null) return;

        double dist = mc.player.distanceTo(target);
        float[] need = angles(target);
        if (need == null) return;

        float dyaw = MathHelper.wrapDegrees(need[0] - mc.player.getYaw());
        float dpitch = need[1] - mc.player.getPitch();
        float angDist = (float) Math.sqrt(dyaw * dyaw + dpitch * dpitch);
        if (angDist > maxFov) return;

        // Adaptive strength: more pull when farther off-target, less when almost on
        float aimStrength;
        if (angDist > 35) aimStrength = 0.42f;
        else if (angDist > 18) aimStrength = 0.34f;
        else if (angDist > 8) aimStrength = 0.28f;
        else aimStrength = 0.18f;

        // Closer targets get more help
        aimStrength *= (float) MathHelper.clamp(1.2 - dist / range * 0.45, 0.7, 1.25);

        if (mc.options.attackKey.isPressed()) {
            aimStrength *= 1.35f;
        }

        aimStrength = Humanizer.aimSmooth(aimStrength);
        RotationUtil.lookAt(target, aimStrength);
    }

    private float[] angles(PlayerEntity target) {
        Vec3d eyes = mc.player.getEyePos();
        double h = 0.7 + MathUtil.randomDouble(-0.03, 0.05);
        Vec3d pos = target.getEntityPos().add(0, target.getHeight() * h, 0);
        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        return new float[]{yaw, MathHelper.clamp(pitch, -90f, 90f)};
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
            if (d > range || d < 0.4) continue;

            float[] need = angles(p);
            if (need == null) continue;
            float dyaw = Math.abs(MathHelper.wrapDegrees(need[0] - mc.player.getYaw()));
            if (dyaw > maxFov + 15) continue;

            // score: prefer center-screen + close
            double score = d * 0.55 + dyaw * 0.08;
            if (score < bestScore) {
                bestScore = score;
                best = p;
            }
        }
        return best;
    }
}
