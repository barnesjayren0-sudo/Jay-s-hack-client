package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

/** Prevent walking off edges — disabled while Scaffold Telly is active. */
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
        clearSneak();
    }

    private void clearSneak() {
        if (forcedSneak && mc.options != null) {
            mc.options.sneakKey.setPressed(false);
            forcedSneak = false;
        }
    }

    private boolean scaffoldTellyActive() {
        if (JayHackClient.moduleManager == null) return false;
        Module sc = JayHackClient.moduleManager.getModuleByName("Scaffold");
        if (sc == null || !sc.isEnabled()) return false;
        if (sc instanceof Scaffold s) {
            return "Telly".equalsIgnoreCase(s.mode.get());
        }
        return true;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.options == null) return;

        // Never fight Telly
        if (scaffoldTellyActive()) {
            clearSneak();
            return;
        }

        if (!mc.player.isOnGround()) {
            clearSneak();
            return;
        }

        BlockPos below = mc.player.getBlockPos().down();
        boolean edge = false;
        double mx = mc.player.getVelocity().x;
        double mz = mc.player.getVelocity().z;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                BlockPos p = below.add(dx, 0, dz);
                if (airCheck.get() && mc.world.getBlockState(p).isAir()) {
                    if (mx * dx + mz * dz > 0.015) edge = true;
                }
            }
        }

        if (sneak.get() && edge) {
            mc.options.sneakKey.setPressed(true);
            forcedSneak = true;
        } else {
            clearSneak();
        }
    }
}
