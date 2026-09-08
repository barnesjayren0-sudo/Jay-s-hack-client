package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationOwner;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * Soft single-target aura — ghost defaults.
 * Prefer TriggerBot + AimAssist on screened servers; keep this OFF if possible.
 */
public class KillAura extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Attack range", 3.05, 2.8, 3.3, 0.05);
    public final NumberSetting fov = new NumberSetting("FOV", "Cone degrees", 50, 25, 90, 5);
    public final BoolSetting weaponsOnly = new BoolSetting("WeaponsOnly", "Sword/axe only", true);
    public final BoolSetting requireClick = new BoolSetting("RequireClick", "Need attack key", true);
    public final BoolSetting comboHit = new BoolSetting("ComboHit", "Use ComboHit timing", true);

    private long lastAttack = 0;
    private int nextDelay = 620;
    private int lockedTargetId = -1;
    private long targetLockedUntil = 0;
    private int aimTick;

    public KillAura() {
        super("KillAura", "Soft single-target (use sparingly)", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_R);
        addSetting(range);
        addSetting(fov);
        addSetting(weaponsOnly);
        addSetting(requireClick);
        addSetting(comboHit);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.combatDelay();
        lastAttack = 0;
        lockedTargetId = -1;
        aimTick = 0;
    }

    @Override
    public void onDisable() {
        lockedTargetId = -1;
        RotationOwner.release("KillAura");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (requireClick.get() && !mc.options.attackKey.isPressed()) return;
        if (Humanizer.shouldSkipTick()) return;
        if (Mobile.shouldThrottle()) return;

        double r = Math.min(range.get(), 3.3);
        if (Reach.isActive()) r = Math.min(r, Reach.getReach() + 0.05);
        float f = Math.min(fov.getFloat(), 90f);

        PlayerEntity target = pickTarget(r, f);
        if (target == null) {
            lockedTargetId = -1;
            return;
        }

        try {
            if (comboHit.get() && !ComboHit.shouldAttack(mc.player, target)) return;
        } catch (Throwable ignored) {}

        aimTick++;
        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) {
            if ((aimTick & 3) == 0 && Humanizer.chance(25)) {
                if (RotationOwner.tryClaim("KillAura", 2, 50))
                    RotationUtil.lookAt(target, ClientSettings.aimSmooth * 0.25f);
            }
            return;
        }

        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.90f) return;

        if (Humanizer.shouldMiss()) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        if (RotationOwner.tryClaim("KillAura", 2, 70))
            RotationUtil.lookAt(target, Math.min(0.28f, ClientSettings.aimSmooth * 0.9f));

        try { ReachHUD.recordHit(mc.player.distanceTo(target)); } catch (Throwable ignored) {}
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }

    private PlayerEntity pickTarget(double range, float fov) {
        long now = System.currentTimeMillis();
        if (lockedTargetId != -1 && now < targetLockedUntil) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p.getId() == lockedTargetId && isInCone(p, range, fov)) return p;
            }
            lockedTargetId = -1;
        }
        PlayerEntity best = TargetUtil.findCombatTarget(range, fov);
        if (best != null) {
            lockedTargetId = best.getId();
            targetLockedUntil = now + Humanizer.combatDelay() + 200L;
        }
        return best;
    }

    private boolean isInCone(PlayerEntity p, double range, float fov) {
        if (p == mc.player || !p.isAlive() || p.isSpectator()) return false;
        if (mc.player.distanceTo(p) > range) return false;
        try { if (AntiBot.isBot(p)) return false; } catch (Throwable ignored) {}
        float yaw = (float) (Math.atan2(p.getZ() - mc.player.getZ(),
                p.getX() - mc.player.getX()) * (180.0 / Math.PI)) - 90f;
        float dyaw = Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
        return dyaw <= fov * 0.5f;
    }
}
