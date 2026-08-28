package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scans LOADED chunks only for player-built patterns (storage, spawners, beacons).
 * Not seed/RNG based — only what is already rendered around you.
 */
public class BaseFinder extends Module {

    private long lastScan;
    private long lastAuto;

    public static final List<Hit> lastHits = new ArrayList<>();

    public BaseFinder() {
        super("BaseFinder", "Loaded-chunk player-build scan", Category.WORLD);
    }

    public static class Hit {
        public final BlockPos pos;
        public final String label;
        public final int score;
        public final double dist;

        Hit(BlockPos pos, String label, int score, double dist) {
            this.pos = pos;
            this.label = label;
            this.score = score;
            this.dist = dist;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        // Soft auto-scan every 12s when enabled
        if (now - lastAuto < 12000) return;
        lastAuto = now;
        scan(false);
    }

    public void scan(boolean force) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (!force && now - lastScan < 4000) return;
        lastScan = now;

        lastHits.clear();
        BlockPos origin = mc.player.getBlockPos();
        int chunkR = Mobile.isSmallScreen() ? 2 : 4;
        ChunkPos oc = new ChunkPos(origin);

        for (int cx = oc.x - chunkR; cx <= oc.x + chunkR; cx++) {
            for (int cz = oc.z - chunkR; cz <= oc.z + chunkR; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                scanChunkEntities(chunk, origin);
                scanChunkBlocks(chunk, origin);
            }
        }

        lastHits.sort(Comparator.comparingInt((Hit h) -> -h.score).thenComparingDouble(h -> h.dist));

        int maxReport = Mobile.isSmallScreen() ? 5 : 12;
        int shown = 0;
        for (Hit h : lastHits) {
            if (shown >= maxReport) break;
            mc.player.sendMessage(Text.literal(String.format(
                    "§8[§bBase§8] §a%s §7score=%d §8@ §f%d %d %d §8(§7%.0fm§8)",
                    h.label, h.score, h.pos.getX(), h.pos.getY(), h.pos.getZ(), h.dist)), false);
            shown++;
        }

        if (!lastHits.isEmpty()) {
            PathToBase.lastTarget = lastHits.get(0).pos;
            mc.player.sendMessage(Text.literal(
                    "§8[§bBase§8] §f" + lastHits.size() + " hits · best → PathToBase / .jay path"), false);
        } else {
            mc.player.sendMessage(Text.literal("§8[§bBase§8] §7No player-build hits in loaded chunks"), false);
        }
    }

    private void scanChunkEntities(WorldChunk chunk, BlockPos origin) {
        for (BlockPos pos : chunk.getBlockEntityPositions()) {
            BlockEntity be = chunk.getBlockEntity(pos);
            if (be == null) continue;
            String label = null;
            int score = 0;
            if (be instanceof ChestBlockEntity) { label = "Chest"; score = 12; }
            else if (be instanceof BarrelBlockEntity) { label = "Barrel"; score = 10; }
            else if (be instanceof ShulkerBoxBlockEntity) { label = "Shulker"; score = 18; }
            else if (be instanceof HopperBlockEntity) { label = "Hopper"; score = 14; }
            else if (be instanceof MobSpawnerBlockEntity) { label = "Spawner"; score = 22; }
            else if (be instanceof BeaconBlockEntity) { label = "Beacon"; score = 30; }
            if (label == null) continue;
            double dist = Math.sqrt(origin.getSquaredDistance(pos));
            // Nearby same-type clusters score higher
            score += clusterBonus(pos, 6);
            lastHits.add(new Hit(pos.toImmutable(), label, score, dist));
        }
    }

    private void scanChunkBlocks(WorldChunk chunk, BlockPos origin) {
        // Sample player-ish blocks inside chunk (not every block — keep light)
        ChunkPos cp = chunk.getPos();
        int minY = Math.max(mc.world.getBottomY(), origin.getY() - 40);
        int maxY = Math.min(mc.world.getTopYInclusive(), origin.getY() + 40);
        for (int x = 0; x < 16; x += 2) {
            for (int z = 0; z < 16; z += 2) {
                for (int y = minY; y <= maxY; y += 3) {
                    BlockPos pos = new BlockPos(cp.getStartX() + x, y, cp.getStartZ() + z);
                    BlockState st = chunk.getBlockState(pos);
                    Block b = st.getBlock();
                    String label = null;
                    int score = 0;
                    if (b == Blocks.CRAFTING_TABLE) { label = "Craft"; score = 8; }
                    else if (b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER) {
                        label = "Furnace"; score = 9;
                    } else if (b == Blocks.ENCHANTING_TABLE) { label = "Enchant"; score = 16; }
                    else if (b == Blocks.ANVIL || b == Blocks.CHIPPED_ANVIL || b == Blocks.DAMAGED_ANVIL) {
                        label = "Anvil"; score = 11;
                    } else if (b == Blocks.ENDER_CHEST) { label = "EnderChest"; score = 15; }
                    else if (isBed(b)) { label = "Bed"; score = 10; }
                    else if (b == Blocks.OBSIDIAN && densityAround(pos, Blocks.OBSIDIAN, 3) >= 4) {
                        label = "Obby"; score = 13;
                    }
                    if (label == null) continue;
                    double dist = Math.sqrt(origin.getSquaredDistance(pos));
                    lastHits.add(new Hit(pos.toImmutable(), label, score, dist));
                }
            }
        }
    }

    private int clusterBonus(BlockPos pos, int r) {
        int n = 0;
        for (Hit h : lastHits) {
            if (h.pos.isWithinDistance(pos, r)) n++;
        }
        return Math.min(10, n * 2);
    }

    private int densityAround(BlockPos pos, Block block, int r) {
        int n = 0;
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int x = -r; x <= r; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -r; z <= r; z++) {
                    m.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (mc.world.getBlockState(m).isOf(block)) n++;
                }
            }
        }
        return n;
    }

    private static boolean isBed(Block b) {
        return b == Blocks.WHITE_BED || b == Blocks.ORANGE_BED || b == Blocks.MAGENTA_BED
                || b == Blocks.LIGHT_BLUE_BED || b == Blocks.YELLOW_BED || b == Blocks.LIME_BED
                || b == Blocks.PINK_BED || b == Blocks.GRAY_BED || b == Blocks.LIGHT_GRAY_BED
                || b == Blocks.CYAN_BED || b == Blocks.PURPLE_BED || b == Blocks.BLUE_BED
                || b == Blocks.BROWN_BED || b == Blocks.GREEN_BED || b == Blocks.RED_BED
                || b == Blocks.BLACK_BED;
    }
}
