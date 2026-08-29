package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Detect 1x1 safe holes (bedrock/obsidian floor + walls). */
public class HoleESP extends Module {

    public static final List<BaseFinder.Hit> holes = new CopyOnWriteArrayList<>();
    public static final int COL_BEDROCK = 0xFF3DDCFF;
    public static final int COL_OBBY = 0xFFFFAA33;

    public final NumberSetting range = new NumberSetting("Range", "Scan radius", 16.0, 6.0, 32.0, 1.0);

    private long lastScan;

    public HoleESP() {
        super("HoleESP", "Highlight safe 1x1 holes", Category.ANARCHY);
        addSetting(range);
    }

    @Override
    public void onDisable() {
        holes.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 400) return;
        lastScan = now;
        scan();
    }

    private void scan() {
        holes.clear();
        BlockPos origin = mc.player.getBlockPos();
        int r = range.getInt();
        int yMin = Math.max(mc.world.getBottomY(), origin.getY() - 6);
        int yMax = Math.min(mc.world.getTopYInclusive(), origin.getY() + 3);

        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = yMin; y <= yMax; y++) {
                    BlockPos feet = new BlockPos(origin.getX() + x, y, origin.getZ() + z);
                    if (!isAir(feet) || !isAir(feet.up())) continue;
                    if (!isHoleBlock(feet.down())) continue;

                    boolean n = isHoleBlock(feet.north());
                    boolean s = isHoleBlock(feet.south());
                    boolean e = isHoleBlock(feet.east());
                    boolean w = isHoleBlock(feet.west());
                    if (!(n && s && e && w)) continue;

                    boolean bedrock = isBedrockish(feet.down())
                            && isBedrockish(feet.north())
                            && isBedrockish(feet.south())
                            && isBedrockish(feet.east())
                            && isBedrockish(feet.west());

                    double dist = Math.sqrt(origin.getSquaredDistance(feet));
                    int color = bedrock ? COL_BEDROCK : COL_OBBY;
                    String label = bedrock ? "BHole" : "OHole";
                    holes.add(new BaseFinder.Hit(feet.toImmutable(), label, bedrock ? 20 : 12, dist, color));
                }
            }
        }
    }

    private boolean isAir(BlockPos p) {
        return mc.world.getBlockState(p).isAir();
    }

    private boolean isHoleBlock(BlockPos p) {
        BlockState st = mc.world.getBlockState(p);
        if (st.isAir()) return false;
        Block b = st.getBlock();
        return b == Blocks.BEDROCK || b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN
                || b == Blocks.ENDER_CHEST || b == Blocks.ANCIENT_DEBRIS || b == Blocks.NETHERITE_BLOCK;
    }

    private boolean isBedrockish(BlockPos p) {
        return mc.world.getBlockState(p).isOf(Blocks.BEDROCK);
    }
}
