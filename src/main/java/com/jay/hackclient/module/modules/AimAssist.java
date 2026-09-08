package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationOwner;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.SilentRotations;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/** Ghost AimAssist — classic soft only by default; yields to KillAura. */
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
    }

    @Override
    public void onDisable() {
        RotationOwner.release("AimAssist");
        SilentRotations.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Mobile.shouldThrottle()) return;
        if (Humanizer.shouldSkipTick()) return;

        try {
            Module sb = com.jay.hackclient.JayHackClient.moduleManager != null
                    ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("SoftBlink") : null;
            if (sb != null && sb.isEnabled()) return;
        } catch (Throwable ignored) {}

        tickCounter++;
        // Aim at most every 2nd tick
        if ((tickCounter & 1) != 0) return;

        double range = ClientSettings.aimRange;
        if (Reach.isActive()) range = Math.min(range, Reach.getReach() + 0.35);

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
        if (dyaw < ClientSettings.aimDeadzone && Humanizer.chance(50)) return;

        boolean attacking = mc.options.attackKey.isPressed();
        if (ClientSettings.requireAttackKey && !attacking) return;

        float strength = Humanizer.aimSmooth(ClientSettings.aimSmooth);
        if (!attacking) strength *= 0.7f;

        if (!RotationOwner.tryClaim("AimAssist", 1, 45)) return;
        RotationUtil.lookAt(target, strength);
    }

    private void silentTick(PlayerEntity target) {
        // Silent is higher risk — keep rare + delayed
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
