package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Creative-like fly for anarchy — disable on servers with AC. */
public class Fly extends Module {

    public final NumberSetting speed = new NumberSetting("Speed", "Fly speed", 0.8, 0.2, 3.0, 0.1);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Up/down speed", 0.6, 0.2, 2.0, 0.1);

    public Fly() {
        super("Fly", "Anarchy fly", Category.ANARCHY);
        addSetting(speed);
        addSetting(vertical);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.getAbilities().flying = false;
            if (!mc.player.isCreative() && !mc.player.isSpectator()) {
                mc.player.getAbilities().allowFlying = false;
            }
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;

        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;

        double spd = speed.get();
        double vert = vertical.get();
        if (mc.options.sprintKey.isPressed()) spd *= 1.6;

        float yaw = mc.player.getYaw() * ((float) Math.PI / 180f);
        double fx = -Math.sin(yaw);
        double fz = Math.cos(yaw);
        double sx = Math.cos(yaw);
        double sz = Math.sin(yaw);

        double mx = 0, mz = 0, my = 0;
        if (mc.options.forwardKey.isPressed()) { mx += fx; mz += fz; }
        if (mc.options.backKey.isPressed()) { mx -= fx; mz -= fz; }
        if (mc.options.leftKey.isPressed()) { mx += sx; mz += sz; }
        if (mc.options.rightKey.isPressed()) { mx -= sx; mz -= sz; }
        if (mc.options.jumpKey.isPressed()) my += vert;
        if (mc.options.sneakKey.isPressed()) my -= vert;

        double len = Math.sqrt(mx * mx + mz * mz);
        if (len > 1e-4) {
            mx = mx / len * spd;
            mz = mz / len * spd;
        }

        mc.player.setVelocity(mx, my, mz);
        mc.player.fallDistance = 0;
        mc.player.setOnGround(false);
    }
}
