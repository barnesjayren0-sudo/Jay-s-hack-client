package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.AngleSmooth;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationOwner;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.SilentRotations;
import com.jay.hackclient.util.TargetTracker;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/** Ghost AimAssist — sticky target + sigmoid smooth (LB-inspired pipeline). */
public class AimAssist extends Module {

    private long lastSilentHit = 0;
    private int silentDelay = 600;
    private int tickCounter = 0;

    public AimAssist() {
        super("AimAssist", "Ghost soft FOV aim — [J]", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onEnable() {
        silentDelay = Humanizer.combatDelay();
        lastSilentHit = 0;
        tickCounter = 0;
        TargetTracker.clear();
    }

    @Override
    public void onDisable() {
        RotationOwner.release("AimAssist");
        SilentRotations.clear();
        TargetTracker.clear();
        setTag(null);
    }

    @Override
    public void onTick() {
        if (!CombatManager.canCombatModulesRun()) return;
        if (mc.player == null || mc.world == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Mobile.shouldThrottle()) return;
        if (Humanizer.shouldSkipTick()) return;

        try {
            Module sb = com.jay.hackclient.JayHackClient.moduleManager != null
                    ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("SoftBlink") : null;
            if (sb != null && sb.isEnabled()) return;
        } catch (Throwable ignored) {}

        tickCounter++;
        if ((tickCounter & 1) != 0) return;

        double range = ClientSettings.aimRange;
        if (Reach.isActive()) range = Math.min(range, Reach.getReach() + 0.35);

        PlayerEntity raw = TargetUtil.findCombatTarget(range, ClientSettings.aimFov);
        PlayerEntity target = TargetTracker.prefer(raw, Humanizer.combatDelay() + 150L);
        if (target == null) {
            setTag(null);
            return;
        }

        // Drop lock if out of extended FOV/range
        if (mc.player.distanceTo(target) > range + 0.6) {
            TargetTracker.clear();
            setTag(null);
            return;
        }

        setTag(String.format("%.1f", mc.player.distanceTo(target)));

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
        if (dyaw < ClientSettings.aimDeadzone && Humanizer.chance(55)) return;

        boolean attacking = mc.options.attackKey.isPressed();
        if (ClientSettings.requireAttackKey && !attacking) return;

        float strength = Humanizer.aimSmooth(ClientSettings.aimSmooth);
        if (!attacking) strength *= 0.65f;

        if (!RotationOwner.tryClaim("AimAssist", 1, 40)) return;
        RotationUtil.lookAt(target, strength, AngleSmooth.Mode.SIGMOID);
    }

    private void silentTick(PlayerEntity target) {
        long now = System.currentTimeMillis();
        if (now - lastSilentHit < silentDelay) return;
        if (!mc.options.attackKey.isPressed()) return;
        if (!RotationOwner.tryClaim("AimAssist", 1, 35)) return;
        float[] ang = SilentRotations.anglesTo(target);
        if (ang == null) return;
        SilentRotations.set(ang[0], ang[1]);
        lastSilentHit = now;
        silentDelay = Humanizer.combatDelay() + 80;
    }
}
