package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.util.math.BlockPos;

/**
 * Edge safety without sticky sneak.
 * Fully disabled while any Scaffold mode is active.
 */
public class SafeWalk extends Module {

    public final BoolSetting sneak = new BoolSetting("Sneak", "Brief sneak near edge", true);
    public final BoolSetting airCheck = new BoolSetting("AirCheck", "Only when edge is air", true);

    private boolean forcedSneak;
    private int sneakTicks;

    public SafeWalk() {
        super("SafeWalk", "Don't walk off edges (non-sticky)", Category.MOVEMENT);
        addSetting(sneak);
        addSetting(airCheck);
    }

    @Override
    public void onDisable() {
        clearSneak();
        sneakTicks = 0;
    }

    private void clearSneak() {
        if (forcedSneak && mc.options != null) {
            // Only release if we forced it — don't steal player's real sneak
            mc.options.sneakKey.setPressed(false);
            forcedSneak = false;
            sneakTicks = 0;
        }
    }

    private boolean scaffoldActive() {
        if (JayHackClient.moduleManager == null) return false;
        Module sc = JayHackClient.moduleManager.getModuleByName("Scaffold");
        return sc != null && sc.isEnabled();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.options == null) return;

        // Never fight Scaffold (any mode)
        if (scaffoldActive()) {
            clearSneak();
            return;
        }

        if (!mc.player.isOnGround() || mc.player.isSneaking() && !forcedSneak) {
            // Player holding sneak themselves — don't touch
            if (!forcedSneak) return;
            clearSneak();
            return;
        }

        BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.2, mc.player.getZ());
        boolean edge = false;

        // Predict next block from look/velocity
        double mx = mc.player.getVelocity().x;
        double mz = mc.player.getVelocity().z;
        double speed = Math.sqrt(mx * mx + mz * mz);

        if (speed > 0.02) {
            int sdx = (int) Math.signum(mx);
            int sdz = (int) Math.signum(mz);
            BlockPos ahead = below.add(sdx, 0, sdz);
            BlockPos ahead2 = below.add(sdx != 0 ? sdx : 0, 0, sdz != 0 ? sdz : 0);
            if (airCheck.get()) {
                if (mc.world.getBlockState(ahead).isAir() || mc.world.getBlockState(ahead2).isAir()) {
                    edge = true;
                }
            } else {
                edge = mc.world.getBlockState(ahead).isAir();
            }
        } else {
            // Standing still — check 4 neighbors under feet offset
            for (int[] d : new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
                BlockPos p = below.add(d[0], 0, d[1]);
                if (mc.world.getBlockState(p).isAir() && mc.world.getBlockState(p.up()).isAir()) {
                    // only if player is near that edge of the block
                    double lx = mc.player.getX() - Math.floor(mc.player.getX()) - 0.5;
                    double lz = mc.player.getZ() - Math.floor(mc.player.getZ()) - 0.5;
                    if (d[0] != 0 && Math.signum(lx) == d[0] && Math.abs(lx) > 0.28) edge = true;
                    if (d[1] != 0 && Math.signum(lz) == d[1] && Math.abs(lz) > 0.28) edge = true;
                }
            }
        }

        if (sneak.get() && edge) {
            mc.options.sneakKey.setPressed(true);
            forcedSneak = true;
            sneakTicks++;
            // Max 3 ticks of forced sneak then release — prevents sticky
            if (sneakTicks > 3) {
                clearSneak();
            }
        } else {
            clearSneak();
        }
    }
}
