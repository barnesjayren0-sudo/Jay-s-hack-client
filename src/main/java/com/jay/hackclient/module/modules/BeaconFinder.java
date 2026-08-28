package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

/** Beacons in loaded chunks — strong base signal. */
public class BeaconFinder extends Module {

    private long lastScan;
    private BlockPos lastTold;

    public BeaconFinder() {
        super("BeaconFinder", "Beacons in loaded chunks", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 3000) return;
        lastScan = now;

        BlockPos origin = mc.player.getBlockPos();
        ChunkPos oc = new ChunkPos(origin);

        for (int cx = oc.x - 3; cx <= oc.x + 3; cx++) {
            for (int cz = oc.z - 3; cz <= oc.z + 3; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (!(be instanceof BeaconBlockEntity)) continue;
                    if (pos.equals(lastTold)) continue;
                    lastTold = pos.toImmutable();
                    PathToBase.lastTarget = lastTold;
                    double dist = Math.sqrt(origin.getSquaredDistance(pos));
                    mc.player.sendMessage(Text.literal(String.format(
                            "§8[§eBeacon§8] §fat §b%d %d %d §8(§7%.0fm§8)",
                            pos.getX(), pos.getY(), pos.getZ(), dist)), false);
                    return;
                }
            }
        }
    }
}
