package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.SilentRotations;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class AimAssist extends Module {

    private long lastSilentHit = 0;
    private int silentDelay = 550;
    private int tickCounter = 0;

    public AimAssist() {
        super("AimAssist", "Soft aim — bind [J]", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onEnable() {
        silentDelay = Humanizer.combatDelay();
        lastSilentHit = 0;
        tickCounter = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Mobile.shouldThrottle()) return;

        tickCounter++;
        if ((tickCounter & 1) != 0) return;

        double range = ClientSettings.aimRange;
        if (Reach.isActive()) range = Math.max(range, Reach.getReach() + 0.8);

        PlayerEntity target = TargetUtil.findCombatTarget(range, ClientSettings.aimFov);
        if (target == null) return;

        if ("silent".equalsIgnoreCase(ClientSettings.aimMode)) {
            silentTick(target);
        } else {
            classicTick(target);
        }
    }

    private void classicTick(PlayerEntity target) {
        float[] ang = SilentRotations.anglesTo(target);
        if (ang == null) return;

        float dyaw = Math.abs(MathHelper.wrapDegrees(ang[0] - mc.player.getYaw()));
        if (dyaw > ClientSettings.aimFov) return;

        boolean attacking = mc.options.attackKey.isPressed();
        if (ClientSettings.requireAttackKey && !attacking) {
            if (dyaw > 22f) return;
            RotationUtil.lookAt(target, 0.10f);
            return;
        }

        float strength = Math.min(0.30f, ClientSettings.aimSmooth * 0.72f);
        if (attacking) strength = Math.min(0.34f, strength + 0.05f);

        // Distance scale — stronger when closer
        double dist = mc.player.distanceTo(target);
        if (dist < 2.5) strength = Math.min(0.36f, strength + 0.04f);

        if (Humanizer.shouldSkipTick()) return;
        RotationUtil.lookAt(target, strength);
    }

    private void silentTick(PlayerEntity target) {
        if (!SilentRotations.inFov(target, ClientSettings.aimFov)) return;
        if (mc.interactionManager == null) return;

        long now = System.currentTimeMillis();
        if (now - lastSilentHit < silentDelay) return;

        boolean attacking = mc.options.attackKey.isPressed() || !ClientSettings.requireAttackKey;
        if (!attacking) return;
        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.88f) return;
        if (Humanizer.shouldMiss()) {
            lastSilentHit = now;
            silentDelay = Humanizer.combatDelay();
            return;
        }

        SilentRotations.silentLookForHit(target, () -> {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        });
        lastSilentHit = now;
        silentDelay = Humanizer.combatDelay();
    }
}
