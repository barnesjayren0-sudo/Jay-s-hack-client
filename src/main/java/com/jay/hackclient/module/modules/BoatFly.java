package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Fly while riding a boat. */
public class BoatFly extends Module {

    public final NumberSetting speed = new NumberSetting("Speed", "Boat fly speed", 1.2, 0.3, 5.0, 0.1);

    public BoatFly() {
        super("BoatFly", "Control boat in air", Category.ANARCHY);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (!(vehicle instanceof BoatEntity boat)) return;

        GameOptions opt = mc.options;
        float spd = speed.getFloat();
        if (opt.sprintKey.isPressed()) spd *= 1.8f;

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
            boat.setVelocity(Vec3d.ZERO);
        } else {
            boat.setVelocity(vx, vy, vz);
        }
        boat.velocityModified = true;
        boat.fallDistance = 0;
    }
}
