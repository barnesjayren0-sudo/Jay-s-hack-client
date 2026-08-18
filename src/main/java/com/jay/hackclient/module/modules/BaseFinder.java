package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class BaseFinder extends Module {

    private long lastScan;

    public BaseFinder() {
        super("BaseFinder", "Scans loaded chunks for storage", Category.WORLD);
    }

    @Override
    public void onTick() {
        // scan on demand only — saves phone CPU
    }

    public void scan(boolean force) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (!force && now - lastScan < 5000) return;
        lastScan = now;

        BlockPos origin = mc.player.getBlockPos();
        int range = Mobile.isSmallScreen() ? 24 : 40; // smaller radius on phone
        int found = 0;
        int maxReport = Mobile.isSmallScreen() ? 4 : 10;

        BlockPos.Mutable m = new BlockPos.Mutable();
        for (int x = -range; x <= range; x += 2) {
            for (int z = -range; z <= range; z += 2) {
                for (int y = -16; y <= 16; y += 2) {
                    m.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    BlockState st = mc.world.getBlockState(m);
                    if (st.isOf(Blocks.CHEST) || st.isOf(Blocks.BARREL)
                            || st.isOf(Blocks.ENDER_CHEST) || st.isOf(Blocks.SHULKER_BOX)
                            || st.isOf(Blocks.SPAWNER)) {
                        found++;
                        if (found == 1) {
                            PathToBase.lastTarget = m.toImmutable();
                        }
                        if (found <= maxReport) {
                            mc.player.sendMessage(Text.literal(
                                    "§8[§bJay§8] §a" + st.getBlock().getName().getString()
                                            + " §7@ " + m.toShortString()), false);
                        }
                    }
                }
            }
        }
        mc.player.sendMessage(Text.literal(
                "§8[§bJay§8] §fScan done · " + found + " hits"
                        + (Mobile.isSmallScreen() ? " §8(phone radius)" : "")), false);
    }
}
