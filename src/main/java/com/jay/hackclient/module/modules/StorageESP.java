package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;

/**
 * Highlights storage via BaseFinder hit colors / WorldEspRenderer.
 * Enable with BaseFinder or alone (filters to storage labels).
 */
public class StorageESP extends Module {

    public StorageESP() {
        super("StorageESP", "Box ESP on chests/shulkers/hoppers", Category.RENDER);
    }

    @Override
    public void onEnable() {
        // Kick a scan so ESP has data immediately
        if (mc.player != null) {
            Module bf = com.jay.hackclient.JayHackClient.moduleManager != null
                    ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("BaseFinder")
                    : null;
            if (bf instanceof BaseFinder finder) {
                finder.scan(false);
            }
        }
    }

    public static boolean isStorage(BlockEntity be) {
        return be instanceof ChestBlockEntity
                || be instanceof EnderChestBlockEntity
                || be instanceof ShulkerBoxBlockEntity
                || be instanceof BarrelBlockEntity
                || be instanceof HopperBlockEntity;
    }
}
