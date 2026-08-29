package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import java.util.HashSet;
import java.util.Set;

/** Report newly loaded chunks (explore / trail). */
public class NewChunks extends Module {

    private final Set<Long> seen = new HashSet<>();
    private long lastReport;

    public NewChunks() {
        super("NewChunks", "Chat when new chunks load", Category.WORLD);
    }

    @Override
    public void onEnable() {
        seen.clear();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        ChunkPos cp = mc.player.getChunkPos();
        int r = 3;
        long now = System.currentTimeMillis();
        int neu = 0;
        int mx = 0, mz = 0;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                int cx = cp.x + x;
                int cz = cp.z + z;
                if (!mc.world.getChunkManager().isChunkLoaded(cx, cz)) continue;
                long key = ChunkPos.toLong(cx, cz);
                if (seen.add(key)) {
                    neu++;
                    mx = cx;
                    mz = cz;
                }
            }
        }
        if (neu > 0 && now - lastReport > 1500) {
            mc.player.sendMessage(Text.literal(
                    "§8[§bJay§8] §7New chunks §f+" + neu + " §8last §f" + mx + "," + mz), false);
            lastReport = now;
        }
        if (seen.size() > 2000) seen.clear();
    }
}
