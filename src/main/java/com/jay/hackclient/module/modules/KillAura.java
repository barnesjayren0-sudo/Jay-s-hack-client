package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class KillAura extends Module {

    private long lastAttack = 0;
    private int nextDelay = 560;
    private int lockedTargetId = -1;
    private long targetLockedUntil = 0;

    public KillAura() {
        super("KillAura", "Quiet aura (use TriggerBot if possible)", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_R);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.combatDelay();
        lastAttack = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick()) return;
        if (Mobile.shouldThrottle()) return;

        LivingEntity target = findTarget();
        if (target == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) {
            if (Humanizer.chance(50)) RotationUtil.lookAt(target, ClientSettings.aimSmooth * 0.5f);
            return;
        }

        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.85f) return;
        if (Humanizer.shouldMiss()) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        RotationUtil.lookAt(target, ClientSettings.aimSmooth * 1.2f);
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }

    private LivingEntity findTarget() {
        LivingEntity best = null;
        double closest = ClientSettings.auraRange;
        long now = System.currentTimeMillis();

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player == mc.player || !player.isAlive() || player.isSpectator()) continue;
            if (AntiBot.isBot(player)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(player.getName().getString())) continue;

            double dist = mc.player.distanceTo(player);
            float yaw = (float) (Math.atan2(player.getZ() - mc.player.getZ(),
                    player.getX() - mc.player.getX()) * (180.0 / Math.PI)) - 90f;
            float yawDiff = Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
            if (dist <= closest && yawDiff <= ClientSettings.auraFov) {
                if (!ClientSettings.auraMultiTarget && lockedTargetId != -1
                        && now < targetLockedUntil && player.getId() != lockedTargetId) {
                    continue;
                }
                closest = dist;
                best = player;
            }
        }
        if (best != null && best.getId() != lockedTargetId) {
            lockedTargetId = best.getId();
            targetLockedUntil = now + Humanizer.combatDelay();
        }
        return best;
    }

    @Override
    public void onDisable() {
        lockedTargetId = -1;
        targetLockedUntil = 0;
    }
}
