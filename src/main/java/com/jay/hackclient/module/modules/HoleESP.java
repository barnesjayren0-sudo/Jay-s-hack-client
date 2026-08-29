package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

/** 1x1 and 2x1 hole ESP for crystal / anarchy. */
public class HoleESP extends Module {

    public static final List<BaseFinder.Hit> holes = new CopyOnWriteArrayList<>();
    public static final int COL_BEDROCK = 0xFF3DDCFF;
    public static final int COL_OBBY = 0xFFFFAA33;
    public static final int COL_DOUBLE = 0xFFAA66FF;

    public final NumberSetting range = new NumberSetting("Range", "Scan radius", 16.0, 6.0, 32.0, 1.0);
    public final BoolSetting doubles = new BoolSetting("2x1", "Detect double holes", true);

    private long lastScan;

    public HoleESP() {
        super("HoleESP", "1x1 and 2x1 safe holes", Category.ANARCHY);
        addSetting(range);
        addSetting(doubles);
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

                    // 1x1
                    if (isHoleBlock(feet.down())
                            && isHoleBlock(feet.north()) && isHoleBlock(feet.south())
                            && isHoleBlock(feet.east()) && isHoleBlock(feet.west())) {
                        boolean bedrock = isBedrockish(feet.down())
                                && isBedrockish(feet.north()) && isBedrockish(feet.south())
                                && isBedrockish(feet.east()) && isBedrockish(feet.west());
                        double dist = Math.sqrt(origin.getSquaredDistance(feet));
                        holes.add(new BaseFinder.Hit(feet.toImmutable(),
                                bedrock ? "BHole" : "OHole",
                                bedrock ? 20 : 12, dist,
                                bedrock ? COL_BEDROCK : COL_OBBY));
                        continue;
                    }

                    // 2x1 doubles (east-west or north-south)
                    if (doubles.get()) {
                        tryDouble(origin, feet, true);
                        tryDouble(origin, feet, false);
                    }
                }
            }
        }
    }

    private void tryDouble(BlockPos origin, BlockPos a, boolean eastWest) {
        BlockPos b = eastWest ? a.east() : a.north();
        if (!isAir(b) || !isAir(b.up())) return;
        if (!isHoleBlock(a.down()) || !isHoleBlock(b.down())) return;

        // Shared long walls + outer ends must be solid
        if (eastWest) {
            if (!isHoleBlock(a.north()) || !isHoleBlock(a.south())) return;
            if (!isHoleBlock(b.north()) || !isHoleBlock(b.south())) return;
            if (!isHoleBlock(a.west()) || !isHoleBlock(b.east())) return;
        } else {
            if (!isHoleBlock(a.east()) || !isHoleBlock(a.west())) return;
            if (!isHoleBlock(b.east()) || !isHoleBlock(b.west())) return;
            if (!isHoleBlock(a.south()) || !isHoleBlock(b.north())) return;
        }

        // Avoid duplicating when scanning the second cell
        if (eastWest && a.getX() > b.getX()) return;
        if (!eastWest && a.getZ() < b.getZ()) return;

        double dist = Math.sqrt(origin.getSquaredDistance(a));
        holes.add(new BaseFinder.Hit(a.toImmutable(), "DHole", 18, dist, COL_DOUBLE));
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
