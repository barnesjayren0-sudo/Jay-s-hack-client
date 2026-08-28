package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scores loaded chunks by player-built density (craft, furnace, bed, obby, etc.).
 */
public class BuildFinder extends Module {

    private long lastScan;

    public BuildFinder() {
        super("BuildFinder", "Player-build density in loaded chunks", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 15000) return;
        lastScan = now;
        scan(false);
    }

    public void scan(boolean force) {
        if (mc.player == null || mc.world == null) return;
        if (!force) {
            long now = System.currentTimeMillis();
            if (now - lastScan < 5000 && lastScan != 0) return;
            lastScan = now;
        }

        List<ChunkScore> scores = new ArrayList<>();
        BlockPos origin = mc.player.getBlockPos();
        int chunkR = Mobile.isSmallScreen() ? 2 : 3;
        ChunkPos oc = new ChunkPos(origin);

        for (int cx = oc.x - chunkR; cx <= oc.x + chunkR; cx++) {
            for (int cz = oc.z - chunkR; cz <= oc.z + chunkR; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                int score = scoreChunk(chunk, origin);
                if (score < 15) continue;
                BlockPos center = new BlockPos(cx * 16 + 8, origin.getY(), cz * 16 + 8);
                double dist = Math.sqrt(origin.getSquaredDistance(center));
                scores.add(new ChunkScore(cx, cz, score, dist, center));
            }
        }

        scores.sort(Comparator.comparingInt((ChunkScore s) -> -s.score));
        int max = Mobile.isSmallScreen() ? 4 : 8;
        int n = 0;
        for (ChunkScore s : scores) {
            if (n >= max) break;
            mc.player.sendMessage(Text.literal(String.format(
                    "§8[§aBuild§8] §fchunk §b%d %d §7score=%d §8(§7%.0fm§8)",
                    s.cx, s.cz, s.score, s.dist)), false);
            n++;
        }

        if (!scores.isEmpty()) {
            PathToBase.lastTarget = scores.get(0).center;
            mc.player.sendMessage(Text.literal(
                    "§8[§aBuild§8] §f" + scores.size() + " hot chunks · path set"), false);
        } else {
            mc.player.sendMessage(Text.literal("§8[§aBuild§8] §7No dense builds nearby"), false);
        }
    }

    private int scoreChunk(WorldChunk chunk, BlockPos origin) {
        int score = 0;
        ChunkPos cp = chunk.getPos();
        int minY = Math.max(mc.world.getBottomY(), origin.getY() - 48);
        int maxY = Math.min(mc.world.getTopYInclusive(), origin.getY() + 48);
        for (int x = 0; x < 16; x += 2) {
            for (int z = 0; z < 16; z += 2) {
                for (int y = minY; y <= maxY; y += 4) {
                    BlockState st = chunk.getBlockState(new BlockPos(cp.getStartX() + x, y, cp.getStartZ() + z));
                    score += blockScore(st.getBlock());
                }
            }
        }
        // Block entities already strong signal
        score += chunk.getBlockEntityPositions().size() * 3;
        return score;
    }

    private int blockScore(Block b) {
        if (b == Blocks.CRAFTING_TABLE) return 6;
        if (b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER) return 5;
        if (b == Blocks.ENCHANTING_TABLE) return 10;
        if (b == Blocks.ANVIL || b == Blocks.CHIPPED_ANVIL || b == Blocks.DAMAGED_ANVIL) return 7;
        if (b == Blocks.ENDER_CHEST) return 8;
        if (b == Blocks.OBSIDIAN) return 2;
        if (b == Blocks.CRYING_OBSIDIAN) return 3;
        if (b == Blocks.NETHERITE_BLOCK) return 12;
        if (b == Blocks.BEACON) return 15;
        if (b == Blocks.RESPAWN_ANCHOR) return 9;
        return 0;
    }

    private static class ChunkScore {
        final int cx, cz, score;
        final double dist;
        final BlockPos center;

        ChunkScore(int cx, int cz, int score, double dist, BlockPos center) {
            this.cx = cx;
            this.cz = cz;
            this.score = score;
            this.dist = dist;
            this.center = center;
        }
    }
}
