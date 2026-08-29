package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Storage container ESP — chests, barrels, shulkers, etc. */
public class StorageESP extends Module {

    public static final List<BaseFinder.Hit> hits = new CopyOnWriteArrayList<>();

    public final NumberSetting range = new NumberSetting("Range", "Chunk radius", 4, 1, 8, 1);
    public final NumberSetting maxMarkers = new NumberSetting("MaxMarkers", "HUD markers", 40, 10, 80, 5);
    public final BoolSetting chests = new BoolSetting("Chests", "Chests", true);
    public final BoolSetting barrels = new BoolSetting("Barrels", "Barrels", true);
    public final BoolSetting shulkers = new BoolSetting("Shulkers", "Shulker boxes", true);
    public final BoolSetting hoppers = new BoolSetting("Hoppers", "Hoppers", false);
    public final BoolSetting ender = new BoolSetting("Ender", "Ender chests", true);
    public final NumberSetting colorR = new NumberSetting("ColorR", "Red", 255, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Green", 200, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Blue", 50, 0, 255, 1);

    private long lastScan;

    public StorageESP() {
        super("StorageESP", "Chest / storage ESP", Category.RENDER);
        addSetting(range);
        addSetting(maxMarkers);
        addSetting(chests);
        addSetting(barrels);
        addSetting(shulkers);
        addSetting(hoppers);
        addSetting(ender);
        addSetting(colorR);
        addSetting(colorG);
        addSetting(colorB);
    }

    public int colorArgb() {
        return 0xFF000000
                | ((colorR.getInt() & 255) << 16)
                | ((colorG.getInt() & 255) << 8)
                | (colorB.getInt() & 255);
    }

    @Override
    public void onDisable() {
        hits.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastScan < 500) return;
        lastScan = now;
        scan();
    }

    private void scan() {
        hits.clear();
        ChunkPos cp = mc.player.getChunkPos();
        int r = range.getInt();
        int color = colorArgb();
        BlockPos playerPos = mc.player.getBlockPos();

        List<BaseFinder.Hit> found = new ArrayList<>();
        for (int cx = -r; cx <= r; cx++) {
            for (int cz = -r; cz <= r; cz++) {
                int x = cp.x + cx;
                int z = cp.z + cz;
                if (!mc.world.getChunkManager().isChunkLoaded(x, z)) continue;
                var chunk = mc.world.getChunk(x, z);
                for (BlockPos pos : chunk.getBlockEntityPositions()) {
                    BlockEntity be = mc.world.getBlockEntity(pos);
                    if (be == null) continue;
                    String label = null;
                    if (chests.get() && be instanceof ChestBlockEntity) label = "Chest";
                    else if (barrels.get() && be instanceof BarrelBlockEntity) label = "Barrel";
                    else if (shulkers.get() && be instanceof ShulkerBoxBlockEntity) label = "Shulker";
                    else if (hoppers.get() && be instanceof HopperBlockEntity) label = "Hopper";
                    else if (ender.get() && be instanceof EnderChestBlockEntity) label = "Ender";
                    if (label == null) continue;
                    double dist = Math.sqrt(playerPos.getSquaredDistance(pos));
                    found.add(new BaseFinder.Hit(pos.toImmutable(), label, 10, dist, color));
                }
            }
        }
        found.sort((a, b) -> Double.compare(a.dist, b.dist));
        int max = maxMarkers.getInt();
        for (int i = 0; i < Math.min(max, found.size()); i++) {
            hits.add(found.get(i));
        }
    }
}
