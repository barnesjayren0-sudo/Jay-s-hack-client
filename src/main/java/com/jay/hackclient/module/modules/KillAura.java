package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/**
 * KillAura [R] — shared TargetUtil + ComboHit gate, FOV cone, sword/axe.
 */
public class KillAura extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Attack range", 3.2, 2.5, 4.5, 0.05);
    public final NumberSetting fov = new NumberSetting("FOV", "Cone degrees", 70, 30, 180, 5);
    public final BoolSetting weaponsOnly = new BoolSetting("WeaponsOnly", "Sword/axe only", true);
    public final BoolSetting comboHit = new BoolSetting("ComboHit", "Use ComboHit timing", true);

    private long lastAttack = 0;
    private int nextDelay = 560;
    private int lockedTargetId = -1;
    private long targetLockedUntil = 0;
    private int aimTick;

    public KillAura() {
        super("KillAura", "Auto attack — bind [R]", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_R);
        addSetting(range);
        addSetting(fov);
        addSetting(weaponsOnly);
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
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick()) return;
        if (Mobile.shouldThrottle()) return;

        double r = effectiveRange();
        float f = fov.getFloat();

        PlayerEntity target = pickTarget(r, f);
        if (target == null) {
            lockedTargetId = -1;
            return;
        }

        // ComboHit: crit vs grounded / punish jump
        if (comboHit.get() && !ComboHit.shouldAttack(mc.player, target)) {
            return;
        }

        aimTick++;
        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) {
            if ((aimTick & 1) == 0 && Humanizer.chance(35)) {
                RotationUtil.lookAt(target, ClientSettings.aimSmooth * 0.35f);
            }
            return;
        }

        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.88f) return;
        if (ClientSettings.critTiming && !CritAssist.canAttackNow(mc.player)
                && comboHit.get()) {
            // allow ComboHit to decide; if CritAssist on and not in window, skip
            if (!mc.player.isOnGround()) return;
        }
        if (Humanizer.shouldMiss()) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        RotationUtil.lookAt(target, Math.min(0.38f, ClientSettings.aimSmooth * 1.05f));
        ReachHUD.recordHit(mc.player.distanceTo(target));
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);

        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
    }

    private double effectiveRange() {
        double r = range.get();
        if (Reach.isActive()) r = Math.min(r, Reach.getReach() + 0.08);
        return r;
    }

    private PlayerEntity pickTarget(double range, float fov) {
        long now = System.currentTimeMillis();

        if (lockedTargetId != -1 && now < targetLockedUntil && !ClientSettings.auraMultiTarget) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p.getId() == lockedTargetId && isInCone(p, range, fov)) {
                    return p;
                }
            }
            lockedTargetId = -1;
        }

        // Shared target system — closest / lowest_hp / crosshair
        PlayerEntity best = TargetUtil.findCombatTarget(range, fov);
        if (best != null) {
            if (best.getId() != lockedTargetId) {
                lockedTargetId = best.getId();
                targetLockedUntil = now + Humanizer.combatDelay() + 180L;
            }
        }
        return best;
    }

    private boolean isInCone(PlayerEntity p, double range, float fov) {
        if (p == mc.player || !p.isAlive() || p.isSpectator()) return false;
        if (mc.player.distanceTo(p) > range) return false;
        float yaw = (float) (Math.atan2(p.getZ() - mc.player.getZ(),
                p.getX() - mc.player.getX()) * (180.0 / Math.PI)) - 90f;
        float dyaw = Math.abs(MathHelper.wrapDegrees(yaw - mc.player.getYaw()));
        return dyaw <= fov;
    }

    @Override
    public void onDisable() {
        lockedTargetId = -1;
        targetLockedUntil = 0;
    }
}
