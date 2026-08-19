package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class AimAssist extends Module {

    public AimAssist() {
        super("AimAssist", "Config-driven soft aim — key J", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick()) return;
        if (Mobile.shouldThrottle()) return;

        PlayerEntity target = findTarget();
        if (target == null) return;

        float[] need = angles(target);
        if (need == null) return;

        float dyaw = MathHelper.wrapDegrees(need[0] - mc.player.getYaw());
        float dpitch = need[1] - mc.player.getPitch();
        float angDist = (float) Math.sqrt(dyaw * dyaw + dpitch * dpitch);
        if (angDist > ClientSettings.aimFov) return;

        double dist = mc.player.distanceTo(target);
        float strength = ClientSettings.aimSmooth;

        if (angDist > 30) strength *= 1.25f;
        else if (angDist < 8) strength *= 0.7f;

        strength *= (float) MathHelper.clamp(1.15 - dist / ClientSettings.aimRange * 0.4, 0.65, 1.2);

        if (ClientSettings.requireAttackKey) {
            if (mc.options.attackKey.isPressed()) strength *= 1.3f;
            else strength *= 0.65f;
        }

        RotationUtil.lookAt(target, Humanizer.aimSmooth(strength));
    }

    private float[] angles(PlayerEntity target) {
        Vec3d eyes = mc.player.getEyePos();
        double h = 0.7 + MathUtil.randomDouble(-0.04, 0.05);
        Vec3d pos = target.getEntityPos().add(0, target.getHeight() * h, 0);
        double dx = pos.x - eyes.x, dy = pos.y - eyes.y, dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        return new float[]{yaw, MathHelper.clamp(pitch, -90f, 90f)};
    }

    private PlayerEntity findTarget() {
        PlayerEntity best = null;
        double bestScore = Double.MAX_VALUE;
        double range = ClientSettings.aimRange;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (AntiBot.isBot(p)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;

            double d = mc.player.distanceTo(p);
            if (d > range || d < 0.35) continue;

            float[] need = angles(p);
            if (need == null) continue;
            float dyaw = Math.abs(MathHelper.wrapDegrees(need[0] - mc.player.getYaw()));
            if (dyaw > ClientSettings.aimFov + 12) continue;

            double score = d * 0.5 + dyaw * 0.09;
            if (score < bestScore) {
                bestScore = score;
                best = p;
            }
        }
        return best;
    }
}
