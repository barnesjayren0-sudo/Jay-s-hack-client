package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.AngleSmooth;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RotationOwner;
import com.jay.hackclient.util.RotationUtil;
import com.jay.hackclient.util.TargetTracker;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

/** Soft single-target aura with sticky tracker (LB-inspired requirements). */
public class KillAura extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Attack range", 3.05, 2.8, 3.3, 0.05);
    public final NumberSetting fov = new NumberSetting("FOV", "Cone degrees", 50, 25, 90, 5);
    public final BoolSetting weaponsOnly = new BoolSetting("WeaponsOnly", "Sword/axe only", true);
    public final BoolSetting requireClick = new BoolSetting("RequireClick", "Need attack key", true);
    public final BoolSetting comboHit = new BoolSetting("ComboHit", "Use ComboHit timing", true);

    private long lastAttack = 0;
    private int nextDelay = 620;
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
        aimTick = 0;
        TargetTracker.clear();
    }

    @Override
    public void onDisable() {
        TargetTracker.clear();
        RotationOwner.release("KillAura");
        setTag(null);
    }

    @Override
    public void onTick() {
        if (!CombatManager.canCombatModulesRun()) return;
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (requireClick.get() && !mc.options.attackKey.isPressed()) return;
        if (Humanizer.shouldSkipTick()) return;
        if (Mobile.shouldThrottle()) return;

        double r = Math.min(range.get(), 3.3);
        if (Reach.isActive()) r = Math.min(r, Reach.getReach() + 0.05);
        float f = Math.min(fov.getFloat(), 90f);

        PlayerEntity raw = TargetUtil.findCombatTarget(r, f);
        PlayerEntity target = TargetTracker.prefer(raw, nextDelay + 200L);
        if (target == null || !isInCone(target, r + 0.4, f + 15f)) {
            TargetTracker.clear();
            setTag(null);
            return;
        }

        setTag(String.format("%.1f", mc.player.distanceTo(target)));

        try {
            if (comboHit.get() && !ComboHit.shouldAttack(mc.player, target)) return;
        } catch (Throwable ignored) {}

        aimTick++;
        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) {
            if ((aimTick & 3) == 0 && Humanizer.chance(20)) {
                if (RotationOwner.tryClaim("KillAura", 2, 45))
                    RotationUtil.lookAt(target, ClientSettings.aimSmooth * 0.22f, AngleSmooth.Mode.SIGMOID);
            }
            return;
        }

        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.90f) return;

        if (Humanizer.shouldMiss()) {
            lastAttack = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        if (RotationOwner.tryClaim("KillAura", 2, 65))
            RotationUtil.lookAt(target, Math.min(0.26f, ClientSettings.aimSmooth * 0.85f), AngleSmooth.Mode.SIGMOID);

        try { ReachHUD.recordHit(mc.player.distanceTo(target)); } catch (Throwable ignored) {}
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
        CombatManager.onAttack();

        lastAttack = now;
        nextDelay = Humanizer.combatDelay();
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
