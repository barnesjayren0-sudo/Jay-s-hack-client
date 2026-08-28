package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Loaded-chunk storage only (chests, barrels, shulkers, hoppers). */
public class StorageFinder extends Module {

    private long lastScan;

    public StorageFinder() {
        super("StorageFinder", "Chests/shulkers in loaded chunks", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 10000) return;
        lastScan = now;
        scan(false);
    }

    public void scan(boolean chat) {
        if (mc.player == null || mc.world == null) return;

        List<BaseFinder.Hit> hits = new ArrayList<>();
        BlockPos origin = mc.player.getBlockPos();
        int chunkR = Mobile.isSmallScreen() ? 2 : 4;
        ChunkPos oc = new ChunkPos(origin);

        for (int cx = oc.x - chunkR; cx <= oc.x + chunkR; cx++) {
            for (int cz = oc.z - chunkR; cz <= oc.z + chunkR; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be == null) continue;
                    String label;
                    int score;
                    if (be instanceof ShulkerBoxBlockEntity) { label = "Shulker"; score = 20; }
                    else if (be instanceof ChestBlockEntity) { label = "Chest"; score = 12; }
                    else if (be instanceof BarrelBlockEntity) { label = "Barrel"; score = 10; }
                    else if (be instanceof HopperBlockEntity) { label = "Hopper"; score = 14; }
                    else continue;
                    double dist = Math.sqrt(origin.getSquaredDistance(pos));
                    hits.add(new BaseFinder.Hit(pos.toImmutable(), label, score, dist));
                }
            }
        }

        hits.sort(Comparator.comparingDouble(h -> h.dist));
        int max = Mobile.isSmallScreen() ? 6 : 15;
        int n = 0;
        for (BaseFinder.Hit h : hits) {
            if (n >= max) break;
            if (chat || isEnabled()) {
                mc.player.sendMessage(Text.literal(String.format(
                        "§8[§6Store§8] §e%s §7%.0fm §8@ §f%d %d %d",
                        h.label, h.dist, h.pos.getX(), h.pos.getY(), h.pos.getZ())), false);
            }
            n++;
        }
        if (!hits.isEmpty()) {
            PathToBase.lastTarget = hits.get(0).pos;
            if (chat) {
                mc.player.sendMessage(Text.literal(
                        "§8[§6Store§8] §f" + hits.size() + " · nearest set for PathToBase"), false);
            }
        } else if (chat) {
            mc.player.sendMessage(Text.literal("§8[§6Store§8] §7No storage in loaded chunks"), false);
        }
    }
}
