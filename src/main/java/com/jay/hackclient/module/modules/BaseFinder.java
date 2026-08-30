package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.DispenserBlockEntity;
import net.minecraft.block.entity.DropperBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Loaded-chunk scanner: storage, farms, utility + colored ESP hits. */
public class BaseFinder extends Module {

    private long lastScan;
    private long lastAuto;

    public static final List<Hit> lastHits = new CopyOnWriteArrayList<>();

    public static boolean drawBoxes = true;
    public static boolean drawTracers = true;
    public static boolean findFarms = true;
    public static boolean findStorage = true;
    public static boolean findUtility = true;
    public static double espRange = 96.0;
    public static int maxEsp = 80;

    public static final int COL_CHEST = 0xFFFFC84A;
    public static final int COL_SHULKER = 0xFFE060FF;
    public static final int COL_HOPPER = 0xFFB0B0B0;
    public static final int COL_SPAWNER = 0xFFFF3030;
    public static final int COL_BEACON = 0xFF40FFFF;
    public static final int COL_FARM = 0xFF40FF60;
    public static final int COL_KELP = 0xFF20C080;
    public static final int COL_UTIL = 0xFF3DDCFF;
    public static final int COL_OBBY = 0xFF555577;
    public static final int COL_DISP = 0xFFFF8840;

    public BaseFinder() {
        super("BaseFinder", "Chests, farms, builds + box/tracer ESP", Category.WORLD);
    }

    public static class Hit {
        public final BlockPos pos;
        public final String label;
        public final int score;
        public final double dist;
        public final int color;

        public Hit(BlockPos pos, String label, int score, double dist, int color) {
            this.pos = pos;
            this.label = label;
            this.score = score;
            this.dist = dist;
            this.color = color;
        }
    }

    @Override
    public void onEnable() {
        scan(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAuto < 7000) return;
        lastAuto = now;
        scan(false);
    }

    @Override
    public void onDisable() {}

    public void scan(boolean force) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        int interval = Mobile.shouldThrottle() ? 5500 : (Mobile.isSmallScreen() ? 4200 : 3000);
        if (!force && now - lastScan < interval) return;
        lastScan = now;

        List<Hit> found = new ArrayList<>();
        BlockPos origin = mc.player.getBlockPos();
        int chunkR = Mobile.isSmallScreen() ? 2 : 4;
        ChunkPos oc = new ChunkPos(origin);

        for (int cx = oc.x - chunkR; cx <= oc.x + chunkR; cx++) {
            for (int cz = oc.z - chunkR; cz <= oc.z + chunkR; cz++) {
                if (!mc.world.isChunkLoaded(cx, cz)) continue;
                WorldChunk chunk = mc.world.getChunk(cx, cz);
                if (findStorage || findUtility) scanChunkEntities(chunk, origin, found);
                scanChunkBlocks(chunk, origin, found);
            }
        }

        found = mergeFarmClusters(found);
        found.sort(Comparator.comparingInt((Hit h) -> -h.score).thenComparingDouble(h -> h.dist));

        lastHits.clear();
        int cap = Mobile.isSmallScreen() ? 48 : 100;
        for (int i = 0; i < Math.min(cap, found.size()); i++) {
            lastHits.add(found.get(i));
        }

        if (force) {
            int maxReport = Mobile.isSmallScreen() ? 6 : 14;
            int shown = 0;
            for (Hit h : lastHits) {
                if (shown >= maxReport) break;
                mc.player.sendMessage(Text.literal(String.format(
                        "§8[§bBase§8] §a%s §7%d §8@ §f%d %d %d §8(§7%.0fm§8)",
                        h.label, h.score, h.pos.getX(), h.pos.getY(), h.pos.getZ(), h.dist)), false);
                shown++;
            }
            if (!lastHits.isEmpty()) {
                PathToBase.lastTarget = lastHits.get(0).pos;
                mc.player.sendMessage(Text.literal(
                        "§8[§bBase§8] §f" + lastHits.size() + " hits · .jay path"), false);
            } else {
                mc.player.sendMessage(Text.literal("§8[§bBase§8] §7No hits in loaded chunks"), false);
            }
        } else if (!lastHits.isEmpty()) {
            PathToBase.lastTarget = lastHits.get(0).pos;
        }
    }

    private void scanChunkEntities(WorldChunk chunk, BlockPos origin, List<Hit> out) {
        for (BlockPos pos : chunk.getBlockEntityPositions()) {
            BlockEntity be = chunk.getBlockEntity(pos);
            if (be == null) continue;
            String label = null;
            int score = 0;
            int color = COL_UTIL;

            if (findStorage) {
                if (be instanceof ChestBlockEntity) { label = "Chest"; score = 15; color = COL_CHEST; }
                else if (be instanceof BarrelBlockEntity) { label = "Barrel"; score = 13; color = COL_CHEST; }
                else if (be instanceof ShulkerBoxBlockEntity) { label = "Shulker"; score = 22; color = COL_SHULKER; }
                else if (be instanceof HopperBlockEntity) { label = "Hopper"; score = 17; color = COL_HOPPER; }
                else if (be instanceof DispenserBlockEntity) { label = "Dispenser"; score = 14; color = COL_DISP; }
                else if (be instanceof DropperBlockEntity) { label = "Dropper"; score = 13; color = COL_DISP; }
            }
            if (label == null && findUtility) {
                if (be instanceof MobSpawnerBlockEntity) { label = "Spawner"; score = 26; color = COL_SPAWNER; }
                else if (be instanceof BeaconBlockEntity) { label = "Beacon"; score = 34; color = COL_BEACON; }
            }
            if (label == null) continue;
            double dist = Math.sqrt(origin.getSquaredDistance(pos));
            score += clusterBonus(out, pos, 6);
            out.add(new Hit(pos.toImmutable(), label, score, dist, color));
        }
    }

    private void scanChunkBlocks(WorldChunk chunk, BlockPos origin, List<Hit> out) {
        ChunkPos cp = chunk.getPos();
        int minY = Math.max(mc.world.getBottomY(), origin.getY() - 56);
        int maxY = Math.min(mc.world.getTopYInclusive(), origin.getY() + 56);

        for (int x = 0; x < 16; x += 2) {
            for (int z = 0; z < 16; z += 2) {
                for (int y = minY; y <= maxY; y += 2) {
                    BlockPos pos = new BlockPos(cp.getStartX() + x, y, cp.getStartZ() + z);
                    BlockState st = chunk.getBlockState(pos);
                    Block b = st.getBlock();
                    String label = null;
                    int score = 0;
                    int color = COL_UTIL;

                    if (findUtility) {
                        if (b == Blocks.CRAFTING_TABLE) { label = "Craft"; score = 8; }
                        else if (b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER) {
                            label = "Furnace"; score = 10;
                        } else if (b == Blocks.ENCHANTING_TABLE) { label = "Enchant"; score = 17; color = COL_BEACON; }
                        else if (b == Blocks.ANVIL || b == Blocks.CHIPPED_ANVIL || b == Blocks.DAMAGED_ANVIL) {
                            label = "Anvil"; score = 12;
                        } else if (b == Blocks.ENDER_CHEST) { label = "EnderChest"; score = 17; color = COL_SHULKER; }
                        else if (isBed(b)) { label = "Bed"; score = 11; }
                        else if (b == Blocks.OBSIDIAN && densityAround(pos, Blocks.OBSIDIAN, 3) >= 5) {
                            label = "Obby"; score = 14; color = COL_OBBY;
                        } else if (b == Blocks.CRYING_OBSIDIAN) { label = "CryingObby"; score = 13; color = COL_OBBY; }
                        else if (b == Blocks.RESPAWN_ANCHOR) { label = "Anchor"; score = 15; color = COL_SPAWNER; }
                        else if (b == Blocks.NETHERITE_BLOCK) { label = "Netherite"; score = 24; color = COL_CHEST; }
                        else if (b == Blocks.LODESTONE) { label = "Lodestone"; score = 12; color = COL_UTIL; }
                    }

                    if (label == null && findFarms) {
                        if (b == Blocks.KELP || b == Blocks.KELP_PLANT) {
                            int d = densityAround(pos, Blocks.KELP, 4) + densityAround(pos, Blocks.KELP_PLANT, 4);
                            if (d >= 5) { label = "KelpFarm"; score = 16 + Math.min(8, d / 3); color = COL_KELP; }
                        } else if (b == Blocks.SUGAR_CANE) {
                            if (densityAround(pos, Blocks.SUGAR_CANE, 3) >= 5) {
                                label = "CaneFarm"; score = 15; color = COL_FARM;
                            }
                        } else if (b == Blocks.BAMBOO) {
                            if (densityAround(pos, Blocks.BAMBOO, 3) >= 5) {
                                label = "BambooFarm"; score = 15; color = COL_FARM;
                            }
                        } else if (b == Blocks.CACTUS) {
                            if (densityAround(pos, Blocks.CACTUS, 3) >= 3) {
                                label = "CactusFarm"; score = 14; color = COL_FARM;
                            }
                        } else if (b == Blocks.NETHER_WART) {
                            if (densityAround(pos, Blocks.NETHER_WART, 3) >= 4) {
                                label = "WartFarm"; score = 16; color = COL_FARM;
                            }
                        } else if (b == Blocks.WHEAT || b == Blocks.CARROTS || b == Blocks.POTATOES
                                || b == Blocks.BEETROOTS) {
                            if (densityAround(pos, b, 3) >= 6) {
                                label = "CropFarm"; score = 13; color = COL_FARM;
                            }
                        } else if (b == Blocks.MELON || b == Blocks.PUMPKIN) {
                            label = "MelonPump"; score = 12; color = COL_FARM;
                        } else if (b == Blocks.FARMLAND) {
                            if (densityAround(pos, Blocks.FARMLAND, 3) >= 6) {
                                label = "Farmland"; score = 11; color = COL_FARM;
                            }
                        } else if (b == Blocks.COMPOSTER) {
                            label = "Composter"; score = 10; color = COL_FARM;
                        }
                    }

                    if (label == null) continue;
                    double dist = Math.sqrt(origin.getSquaredDistance(pos));
                    out.add(new Hit(pos.toImmutable(), label, score, dist, color));
                }
            }
        }
    }

    private List<Hit> mergeFarmClusters(List<Hit> in) {
        List<Hit> out = new ArrayList<>();
        boolean[] used = new boolean[in.size()];
        for (int i = 0; i < in.size(); i++) {
            if (used[i]) continue;
            Hit a = in.get(i);
            boolean farmish = a.label.contains("Farm") || a.label.equals("Farmland")
                    || a.label.equals("MelonPump") || a.label.equals("Composter");
            if (!farmish) {
                out.add(a);
                used[i] = true;
                continue;
            }
            int count = 1;
            int bestScore = a.score;
            BlockPos best = a.pos;
            double bestDist = a.dist;
            for (int j = i + 1; j < in.size(); j++) {
                if (used[j]) continue;
                Hit b = in.get(j);
                if (!b.label.equals(a.label)) continue;
                if (!a.pos.isWithinDistance(b.pos, 12)) continue;
                used[j] = true;
                count++;
                if (b.score > bestScore) {
                    bestScore = b.score;
                    best = b.pos;
                    bestDist = b.dist;
                }
            }
            used[i] = true;
            out.add(new Hit(best, a.label + (count > 1 ? "x" + count : ""),
                    bestScore + count, bestDist, a.color));
        }
        return out;
    }

    private int clusterBonus(List<Hit> list, BlockPos pos, int r) {
        int n = 0;
        for (Hit h : list) {
            if (h.pos.isWithinDistance(pos, r)) n++;
        }
        return Math.min(12, n * 2);
    }

    private int densityAround(BlockPos pos, Block block, int r) {
        int n = 0;
        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int x = -r; x <= r; x++) {
            for (int y = -2; y <= 2; y++) {
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
