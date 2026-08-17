package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class PortalFinder extends Module {

    private long lastScan = 0;
    private BlockPos lastPortal = null;

    public PortalFinder() {
        super("PortalFinder", "Detects nether portal blocks nearby", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 3000) return;
        lastScan = now;

        BlockPos origin = mc.player.getBlockPos();
        int r = 24;

        for (int x = -r; x <= r; x += 2) {
            for (int y = -8; y <= 8; y += 2) {
                for (int z = -r; z <= r; z += 2) {
                    BlockPos pos = origin.add(x, y, z);
                    if (!mc.world.getBlockState(pos).isOf(Blocks.NETHER_PORTAL)) continue;
                    if (pos.equals(lastPortal)) return;

                    lastPortal = pos;
                    double dist = Math.sqrt(origin.getSquaredDistance(pos));
                    mc.player.sendMessage(Text.literal(
                            String.format("§8[§5Portal§8] §fat §b%d %d %d §8(§7%.0fm§8)",
                                    pos.getX(), pos.getY(), pos.getZ(), dist)
                    ), false);
                    return;
                }
            }
        }
    }
}
