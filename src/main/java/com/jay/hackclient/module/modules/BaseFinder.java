package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.entity.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashSet;
import java.util.Set;

public class BaseFinder extends Module {

    private long lastScan = 0;
    private final Set<BlockPos> reported = new HashSet<>();
    private final int scanIntervalMs = 2500;
    private final int maxReports = 8;

    public BaseFinder() {
        super("BaseFinder", "Finds chests, spawners, shulkers in loaded chunks", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScan < scanIntervalMs) return;
        lastScan = now;

        scan(false);
    }

    public void scan(boolean forceChat) {
        if (mc.world == null || mc.player == null) return;

        int found = 0;
        BlockPos playerPos = mc.player.getBlockPos();
        ChunkPos origin = new ChunkPos(playerPos);

        int radius = 3; // chunks
        for (int cx = origin.x - radius; cx <= origin.x + radius; cx++) {
            for (int cz = origin.z - radius; cz <= origin.z + radius; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);

                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    BlockEntity be = chunk.getBlockEntity(pos);
                    if (be == null) continue;

                    String type = classify(be);
                    if (type == null) continue;
                    if (reported.contains(pos) && !forceChat) continue;

                    reported.add(pos);
                    double dist = Math.sqrt(playerPos.getSquaredDistance(pos));

                    if (found < maxReports || forceChat) {
                        mc.player.sendMessage(Text.literal(
                                String.format("§8[§dBase§8] §f%s §7at §b%d %d %d §8(§7%.0fm§8)",
                                        type, pos.getX(), pos.getY(), pos.getZ(), dist)
                        ), false);
                        found++;
                    }
                }
            }
        }

        if (forceChat && found == 0) {
            mc.player.sendMessage(Text.literal("§8[§dBase§8] §7No storage/spawners in loaded chunks"), false);
        }

        // Prevent unbounded growth
        if (reported.size() > 400) reported.clear();
    }

    private String classify(BlockEntity be) {
        if (be instanceof MobSpawnerBlockEntity) return "§cSpawner";
        if (be instanceof ChestBlockEntity) return "§6Chest";
        if (be instanceof EnderChestBlockEntity) return "§5EnderChest";
        if (be instanceof ShulkerBoxBlockEntity) return "§dShulker";
        if (be instanceof BarrelBlockEntity) return "§eBarrel";
        if (be instanceof HopperBlockEntity) return "§7Hopper";
        if (be instanceof FurnaceBlockEntity || be instanceof BlastFurnaceBlockEntity || be instanceof SmokerBlockEntity)
            return "§8Furnace";
        return null;
    }

    @Override
    public void onDisable() {
        reported.clear();
    }
}
