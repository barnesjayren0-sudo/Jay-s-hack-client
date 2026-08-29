package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

/** Prevent walking off edges — non-sticky sneak. */
public class SafeWalk extends Module {

    public final BoolSetting sneak = new BoolSetting("Sneak", "Brief sneak near edge", true);
    public final BoolSetting airCheck = new BoolSetting("AirCheck", "Only when edge is air", true);

    private boolean forcedSneak;

    public SafeWalk() {
        super("SafeWalk", "Don't walk off edges", Category.MOVEMENT);
        addSetting(sneak);
        addSetting(airCheck);
    }

    @Override
    public void onDisable() {
        if (forcedSneak && mc.options != null) {
            mc.options.sneakKey.setPressed(false);
            forcedSneak = false;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.options == null) return;
        if (!mc.player.isOnGround()) {
            if (forcedSneak) {
                mc.options.sneakKey.setPressed(false);
                forcedSneak = false;
            }
            return;
        }

        BlockPos below = mc.player.getBlockPos().down();
        boolean edge = false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos p = below.add(dx, 0, dz);
                if (airCheck.get() && mc.world.getBlockState(p).isAir()) {
                    // near edge relative to movement
                    double mx = mc.player.getVelocity().x;
                    double mz = mc.player.getVelocity().z;
                    if (mx * dx + mz * dz > 0.01) edge = true;
                }
            }
        }

        if (sneak.get() && edge) {
            mc.options.sneakKey.setPressed(true);
            forcedSneak = true;
        } else if (forcedSneak) {
            mc.options.sneakKey.setPressed(false);
            forcedSneak = false;
        }
    }
}
