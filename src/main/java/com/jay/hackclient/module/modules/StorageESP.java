package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

public class StorageESP extends Module {

    public StorageESP() {
        super("StorageESP", "Highlights nearby storage blocks", Category.RENDER);
    }

    @Override
    public void onTick() {
        // Visual highlighting of block entities needs world render mixins.
        // This module flags intent; chests are detected for future render pass.
        if (mc.world == null || mc.player == null) return;
        // Keep lightweight — full box ESP comes with render mixin.
    }

    public static boolean isStorage(BlockEntity be) {
        return be instanceof ChestBlockEntity
                || be instanceof EnderChestBlockEntity
                || be instanceof ShulkerBoxBlockEntity;
    }
}
