package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import com.jay.hackclient.util.SilentRotations;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** AimAssist — real packets only. Smooth / Silent / Snap. */
public class AimAssist extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Aim style", "Smooth", "Smooth", "Silent", "Snap");
    public final NumberSetting range = new NumberSetting("Range", "Assist range", 4.5, 2.5, 8.0, 0.1);
    public final NumberSetting fov = new NumberSetting("FOV", "Assist FOV", 60, 10, 180, 5);
    public final NumberSetting speed = new NumberSetting("Speed", "Aim speed", 0.35, 0.05, 1.0, 0.05);
    public final BoolSetting playersOnly = new BoolSetting("Players Only", "Only players", true);
    public final BoolSetting weaponsOnly = new BoolSetting("Weapons Only", "Only when holding weapon", false);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Send vanilla look packets", true);

    public AimAssist() {
        super("AimAssist", "Smooth / silent aim assist", Category.COMBAT);
        addSetting(mode); addSetting(range); addSetting(fov); addSetting(speed);
        addSetting(playersOnly); addSetting(weaponsOnly); addSetting(realPackets);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (weaponsOnly.get()) {
            String n = mc.player.getMainHandStack().getItem().toString().toLowerCase();
            if (!n.contains("sword") && !n.contains("axe")) return;
        }
        PlayerEntity target = null;
        try { target = TargetUtil.findCombatTarget(range.get(), fov.getFloat()); } catch (Throwable ignored) {}
        if (target == null) target = nearest(range.get(), fov.getFloat());
        if (target == null) { setTag(null); return; }
        try {
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(target.getName().getString())) return;
        } catch (Throwable ignored) {}
        setTag(target.getName().getString());
        float[] ang = anglesTo(target);
        if (ang == null) return;
        if ("Silent".equals(mode.get())) {
            if (realPackets.get()) RealPackets.sendLook(ang[0], ang[1], mc.player.isOnGround());
            return;
        }
        float smooth = "Snap".equals(mode.get()) ? 1.0f : (float) speed.get();
        float curYaw = mc.player.getYaw(), curPitch = mc.player.getPitch();
        float dyaw = MathHelper.wrapDegrees(ang[0] - curYaw);
        float dpitch = ang[1] - curPitch;
        float newYaw = curYaw + dyaw * smooth;
        float newPitch = MathHelper.clamp(curPitch + dpitch * smooth, -90f, 90f);
        mc.player.setYaw(newYaw);
        mc.player.setPitch(newPitch);
        if (realPackets.get()) RealPackets.sendLook(newYaw, newPitch, mc.player.isOnGround());
    }

    private PlayerEntity nearest(double range, float fovDeg) {
        PlayerEntity best = null; double bestD = range;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            double d = mc.player.distanceTo(p);
            if (d > bestD) continue;
            float[] a = anglesTo(p);
            if (a == null) continue;
            if (Math.abs(MathHelper.wrapDegrees(a[0] - mc.player.getYaw())) > fovDeg * 0.5f) continue;
            bestD = d; best = p;
        }
        return best;
    }

    private float[] anglesTo(PlayerEntity target) {
        try { float[] a = SilentRotations.anglesTo(target); if (a != null) return a; } catch (Throwable ignored) {}
        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = target.getPos().add(0, target.getHeight() * 0.9, 0);
        double dx = pos.x - eyes.x, dy = pos.y - eyes.y, dz = pos.z - eyes.z;
        double h = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float pitch = (float) -(MathHelper.atan2(dy, h) * (180.0 / Math.PI));
        return new float[]{yaw, MathHelper.clamp(pitch, -90f, 90f)};
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
