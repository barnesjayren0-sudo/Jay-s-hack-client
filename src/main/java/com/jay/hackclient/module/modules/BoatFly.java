package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.Vec3d;

/** Fly while riding a boat. */
public class BoatFly extends Module {

    public final NumberSetting speed = new NumberSetting("Speed", "Horizontal speed", 1.2, 0.3, 3.0, 0.1);
    public final NumberSetting vertical = new NumberSetting("Vertical", "Up/down speed", 0.4, 0.1, 1.5, 0.05);

    public BoatFly() {
        super("BoatFly", "Fly with boat", Category.ANARCHY);
        addSetting(speed);
        addSetting(vertical);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        Entity veh = mc.player.getVehicle();
        if (!(veh instanceof BoatEntity boat)) return;

        double sp = speed.get();
        double vp = vertical.get();

        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        double x = 0, z = 0;
        if (mc.options.forwardKey.isPressed()) {
            x -= Math.sin(rad) * sp;
            z += Math.cos(rad) * sp;
        }
        if (mc.options.backKey.isPressed()) {
            x += Math.sin(rad) * sp;
            z -= Math.cos(rad) * sp;
        }
        if (mc.options.leftKey.isPressed()) {
            x += Math.cos(rad) * sp;
            z += Math.sin(rad) * sp;
        }
        if (mc.options.rightKey.isPressed()) {
            x -= Math.cos(rad) * sp;
            z -= Math.sin(rad) * sp;
        }

        double y = boat.getVelocity().y;
        if (mc.options.jumpKey.isPressed()) y = vp;
        else if (mc.options.sneakKey.isPressed()) y = -vp;
        else y = 0;

        boat.setVelocity(new Vec3d(x, y, z));
    }
}
