package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.RealPackets;
import com.jay.hackclient.util.SilentRotations;
import com.jay.hackclient.util.TargetTracker;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * KillAura — real packets only.
 * FIRST PERSON → Silent: look away and still hit (look packets to target, camera free).
 * THIRD PERSON → Head tracks the enemy (F5 shows head locked on).
 * Rotate mode Auto switches between Silent / Track by perspective.
 */
public class KillAura extends Module {

    public final ModeSetting rotateMode = new ModeSetting(
            "Rotate", "How rotations work", "Auto",
            "Auto", "Silent", "Track", "None"
    );
    public final NumberSetting range = new NumberSetting("Range", "Attack range", 3.1, 2.5, 6.0, 0.05);
    public final NumberSetting fov = new NumberSetting("FOV", "Target cone (0 = 360)", 180, 0, 360, 5);
    public final NumberSetting aps = new NumberSetting("APS", "Attacks per second soft cap", 8, 1, 20, 1);
    public final NumberSetting slotSmooth = new NumberSetting("Smooth", "Head track smoothing", 0.45, 0.05, 1.0, 0.05);
    public final BoolSetting weaponsOnly = new BoolSetting("Weapons Only", "Sword/axe only", true);
    public final BoolSetting requireClick = new BoolSetting("Require Click", "Need attack key held", false);
    public final BoolSetting playersOnly = new BoolSetting("Players Only", "Only attack players", true);
    public final BoolSetting throughWalls = new BoolSetting("Through Walls", "Ignore line of sight", false);
    public final BoolSetting cooldownCheck = new BoolSetting("Cooldown", "Wait for attack cooldown", true);
    public final BoolSetting swing = new BoolSetting("Swing", "Swing hand on hit", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Use vanilla C2S packets only", true);

    private long lastAttack;
    private int nextDelay = 120;
    private PlayerEntity currentTarget;
    private float silentYaw, silentPitch;
    private boolean hasSilent;

    public KillAura() {
        super("KillAura", "Silent 1st-person / head-track 3rd-person aura", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_R);
        addSetting(rotateMode); addSetting(range); addSetting(fov); addSetting(aps);
        addSetting(slotSmooth); addSetting(weaponsOnly); addSetting(requireClick);
        addSetting(playersOnly); addSetting(throughWalls); addSetting(cooldownCheck);
        addSetting(swing); addSetting(realPackets);
    }

    @Override
    public void onEnable() {
        lastAttack = 0;
        nextDelay = Math.max(50, (int) (1000.0 / aps.get()));
        currentTarget = null;
        hasSilent = false;
        TargetTracker.clear();
    }

    @Override
    public void onDisable() {
        currentTarget = null;
        hasSilent = false;
        TargetTracker.clear();
        setTag(null);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        try { if (!CombatManager.canCombatModulesRun()) return; } catch (Throwable ignored) {}
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) { setTag(null); return; }
        if (requireClick.get() && !mc.options.attackKey.isPressed()) return;
        try {
            if (Humanizer.shouldSkipTick()) return;
            if (Mobile.shouldThrottle()) return;
        } catch (Throwable ignored) {}

        double r = range.get();
        float f = fov.getFloat();
        if (f <= 0) f = 360f;

        PlayerEntity raw = findTarget(r, f);
        PlayerEntity target;
        try { target = TargetTracker.prefer(raw, nextDelay + 200L); }
        catch (Throwable t) { target = raw; }
        currentTarget = target;

        if (target == null || !target.isAlive()) { setTag(null); hasSilent = false; return; }
        setTag(String.format("%.1fm", mc.player.distanceTo(target)));

        boolean thirdPerson = isThirdPerson();
        String rot = resolveRotateMode(thirdPerson);
        float[] ang = anglesTo(target);
        if (ang == null) return;

        switch (rot) {
            case "Silent" -> applySilent(ang);
            case "Track" -> applyTrack(ang);
            default -> {}
        }

        long now = System.currentTimeMillis();
        if (now - lastAttack < nextDelay) return;

        if (cooldownCheck.get()) {
            try {
                float cd = mc.player.getAttackCooldownProgress(0.5f);
                float min = 0.85f;
                try { if (ClientSettings.cooldownCheck) min = 0.90f; } catch (Throwable ignored) {}
                if (cd < min) return;
            } catch (Throwable ignored) {}
        }

        try {
            if (Humanizer.shouldMiss()) {
                lastAttack = now;
                nextDelay = Math.max(50, (int) (1000.0 / aps.get()) + Humanizer.combatDelay() / 10);
                return;
            }
        } catch (Throwable ignored) {}

        if (realPackets.get() && !"None".equals(rot)) {
            RealPackets.sendLook(ang[0], ang[1], mc.player.isOnGround());
        }

        doAttack(target);

        lastAttack = now;
        nextDelay = Math.max(50, (int) (1000.0 / aps.get()));
        try { nextDelay = Math.max(nextDelay, Humanizer.combatDelay() / 8); } catch (Throwable ignored) {}
        try { CombatManager.onAttack(); } catch (Throwable ignored) {}
        try { ReachHUD.recordHit(mc.player.distanceTo(target)); } catch (Throwable ignored) {}
    }

    private boolean isThirdPerson() {
        try { return mc.options.getPerspective() != Perspective.FIRST_PERSON; }
        catch (Throwable t) { return false; }
    }

    private String resolveRotateMode(boolean thirdPerson) {
        String m = rotateMode.get();
        if ("Auto".equals(m)) return thirdPerson ? "Track" : "Silent";
        return m;
    }

    /** 1st person: server looks at target, your camera stays free — hit while looking away. */
    private void applySilent(float[] ang) {
        silentYaw = ang[0]; silentPitch = ang[1]; hasSilent = true;
        if (realPackets.get()) RealPackets.sendLook(ang[0], ang[1], mc.player.isOnGround());
    }

    /** 3rd person: head smoothly tracks enemy so F5 shows lock-on. */
    private void applyTrack(float[] ang) {
        float smooth = (float) slotSmooth.get();
        float curYaw = mc.player.getYaw(), curPitch = mc.player.getPitch();
        float dyaw = MathHelper.wrapDegrees(ang[0] - curYaw);
        float dpitch = ang[1] - curPitch;
        float newYaw = curYaw + dyaw * smooth;
        float newPitch = MathHelper.clamp(curPitch + dpitch * smooth, -90f, 90f);
        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
        if (realPackets.get()) RealPackets.sendLook(newYaw, newPitch, mc.player.isOnGround());
    }

    private void doAttack(PlayerEntity target) {
        if (realPackets.get()) {
            RealPackets.attackEntity(target);
        } else if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            if (swing.get()) mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private PlayerEntity findTarget(double range, float fovDeg) {
        PlayerEntity best = null;
        double bestDist = range + 0.01;
        try {
            PlayerEntity util = TargetUtil.findCombatTarget(range, fovDeg >= 360 ? 360 : fovDeg);
            if (util != null && isValid(util, range, fovDeg)) return util;
        } catch (Throwable ignored) {}
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (!isValid(p, range, fovDeg)) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestDist) { bestDist = d; best = p; }
        }
        return best;
    }

    private boolean isValid(PlayerEntity p, double range, float fovDeg) {
        if (p == null || p == mc.player || !p.isAlive() || p.isSpectator()) return false;
        if (mc.player.distanceTo(p) > range) return false;
        try { if (AntiBot.isBot(p)) return false; } catch (Throwable ignored) {}
        try {
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) return false;
        } catch (Throwable ignored) {}
        if (!throughWalls.get()) {
            try { if (!mc.player.canSee(p)) return false; } catch (Throwable ignored) {}
        }
        if (fovDeg < 360f) {
            float[] ang = anglesTo(p);
            if (ang == null) return false;
            if (Math.abs(MathHelper.wrapDegrees(ang[0] - mc.player.getYaw())) > fovDeg * 0.5f) return false;
        }
        return true;
    }

    private float[] anglesTo(PlayerEntity target) {
        try {
            float[] a = SilentRotations.anglesTo(target);
            if (a != null) return a;
        } catch (Throwable ignored) {}
        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.85, 0);
        double dx = pos.x - eyes.x, dy = pos.y - eyes.y, dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, horiz) * (180.0 / Math.PI));
        return new float[]{yaw, MathHelper.clamp(pitch, -90f, 90f)};
    }

    private void setTag(String t) {
        try {
            var f = Module.class.getDeclaredField("tag");
            f.setAccessible(true);
            f.set(this, t);
        } catch (Throwable ignored) {}
    }

    public PlayerEntity getCurrentTarget() { return currentTarget; }
    public boolean hasSilentAngles() { return hasSilent; }
    public float getSilentYaw() { return silentYaw; }
    public float getSilentPitch() { return silentPitch; }
}
