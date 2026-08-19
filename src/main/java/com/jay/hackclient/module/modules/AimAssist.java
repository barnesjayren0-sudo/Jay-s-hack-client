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

    public AimAssist() {
        super("AimAssist", "classic/silent aim — key J", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick()) return;
        if (Mobile.shouldThrottle()) return;

        PlayerEntity target = TargetUtil.findCombatTarget(ClientSettings.aimRange, ClientSettings.aimFov);
        if (target == null) return;

        if ("silent".equalsIgnoreCase(ClientSettings.aimMode)) {
            silentTick(target);
        } else {
            classicTick(target);
        }
    }

    private void classicTick(PlayerEntity target) {
        float dyaw = Math.abs(MathHelper.wrapDegrees(
                (SilentRotations.anglesTo(target) != null ? SilentRotations.anglesTo(target)[0] : mc.player.getYaw())
                        - mc.player.getYaw()));
        if (dyaw > ClientSettings.aimFov) return;

        float strength = ClientSettings.aimSmooth;
        if (ClientSettings.requireAttackKey) {
            strength *= mc.options.attackKey.isPressed() ? 1.3f : 0.65f;
        }
        RotationUtil.lookAt(target, Humanizer.aimSmooth(strength));
    }

    private void silentTick(PlayerEntity target) {
        if (!SilentRotations.inFov(target, ClientSettings.aimFov)) return;
        if (mc.interactionManager == null) return;

        long now = System.currentTimeMillis();
        if (now - lastSilentHit < silentDelay) return;

        boolean attacking = mc.options.attackKey.isPressed() || !ClientSettings.requireAttackKey;
        if (!attacking) return;
        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.9f) return;
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
