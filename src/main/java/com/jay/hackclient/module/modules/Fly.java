package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/** Client-side fly for anarchy — WASD + Space/Shift. */
public class Fly extends Module {

    public final NumberSetting speed = new NumberSetting("Speed", "Fly speed", 1.0, 0.2, 5.0, 0.1);

    public Fly() {
        super("Fly", "Creative-style flight", Category.ANARCHY);
        setKeyBind(GLFW.GLFW_KEY_G);
        addSetting(speed);
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.player.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        GameOptions opt = mc.options;
        float spd = speed.getFloat();
        if (opt.sprintKey.isPressed()) spd *= 2.0f;

        float yaw = mc.player.getYaw() * ((float) Math.PI / 180f);
        double forward = 0, strafe = 0, up = 0;

        if (opt.forwardKey.isPressed()) forward += 1;
        if (opt.backKey.isPressed()) forward -= 1;
        if (opt.leftKey.isPressed()) strafe += 1;
        if (opt.rightKey.isPressed()) strafe -= 1;
        if (opt.jumpKey.isPressed()) up += 1;
        if (opt.sneakKey.isPressed()) up -= 1;

        double sin = MathHelper.sin(yaw);
        double cos = MathHelper.cos(yaw);

        double vx = (strafe * cos - forward * sin) * spd;
        double vz = (forward * cos + strafe * sin) * spd;
        double vy = up * spd;

        if (forward == 0 && strafe == 0 && up == 0) {
            mc.player.setVelocity(0, 0, 0);
        } else {
            mc.player.setVelocity(vx, vy, vz);
        }
        mc.player.fallDistance = 0;
        mc.player.setOnGround(false);
    }
}
