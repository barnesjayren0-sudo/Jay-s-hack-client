package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Slow circle-strafe around target while attacking (legit). */
public class TargetStrafe extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Engage range", 3.5, 2.0, 5.0, 0.1);
    public final NumberSetting speed = new NumberSetting("Speed", "Strafe strength", 0.18, 0.08, 0.35, 0.01);
    public final NumberSetting fov = new NumberSetting("FOV", "Only when facing target", 90, 40, 180, 5);

    private int dir = 1;

    public TargetStrafe() {
        super("TargetStrafe", "Circle target slowly", Category.COMBAT);
        addSetting(range);
        addSetting(speed);
        addSetting(fov);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!mc.options.forwardKey.isPressed() && !mc.player.isSprinting()) return;

        PlayerEntity t = TargetUtil.find(range.get(), fov.getFloat());
        if (t == null) return;

        // Bounce off walls
        if (mc.player.horizontalCollision) dir = -dir;

        double dx = t.getX() - mc.player.getX();
        double dz = t.getZ() - mc.player.getZ();
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0;

        float rad = (float) Math.toRadians(yaw + 90 * dir);
        double sp = speed.get();
        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(
                v.x * 0.6 + MathHelper.sin(-rad) * sp,
                v.y,
                v.z * 0.6 + MathHelper.cos(rad) * sp
        );
    }
}
