package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class SpawnerFinder extends Module {

    private long lastScan = 0;
    private BlockPos lastTold = null;

    public SpawnerFinder() {
        super("SpawnerFinder", "Alerts when spawners are nearby", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 2000) return;
        lastScan = now;

        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos origin = new ChunkPos(playerPos);

        for (int cx = origin.x - 2; cx <= origin.x + 2; cx++) {
            for (int cz = origin.z - 2; cz <= origin.z + 2; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (!(be instanceof MobSpawnerBlockEntity)) continue;
                    if (pos.equals(lastTold)) continue;

                    double dist = Math.sqrt(playerPos.getSquaredDistance(pos));
                    if (dist > 64) continue;

                    lastTold = pos;
                    mc.player.sendMessage(Text.literal(
                            String.format("§8[§cSpawner§8] §fat §b%d %d %d §8(§7%.0fm§8)",
                                    pos.getX(), pos.getY(), pos.getZ(), dist)
                    ), false);
                    return;
                }
            }
        }
    }
}
