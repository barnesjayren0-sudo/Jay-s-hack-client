package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Light inventory QoL — drop junk with lag-safe delays. */
public class InvSort extends Module {

    public final NumberSetting delay = new NumberSetting("Delay", "Ms between clicks", 80, 20, 300, 10);
    public final BoolSetting dropJunk = new BoolSetting("DropJunk", "Drop cobble/dirt/netherrack", true);

    private long lastClick;

    public InvSort() {
        super("InvSort", "Drop junk / tidy hotbar slowly", Category.PLAYER);
        addSetting(delay);
        addSetting(dropJunk);
    }

    @Override
    public void onTick() {
        if (!dropJunk.get()) return;
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < delay.getInt()) return;

        // Main inventory slots 9-35 in player inventory screen mapping:
        // When no screen open, use creative/player inventory via clickSlot on syncId 0
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            if (!isJunk(stack)) continue;

            // slot index in PlayerScreenHandler: main inv 9-35 map to same
            try {
                mc.interactionManager.clickSlot(
                        mc.player.playerScreenHandler.syncId,
                        i,
                        1, // Q drop one
                        SlotActionType.THROW,
                        mc.player
                );
                lastClick = now;
                return;
            } catch (Throwable ignored) {
                return;
            }
        }
    }

    private boolean isJunk(ItemStack s) {
        return s.isOf(Items.COBBLESTONE)
                || s.isOf(Items.DIRT)
                || s.isOf(Items.NETHERRACK)
                || s.isOf(Items.GRANITE)
                || s.isOf(Items.DIORITE)
                || s.isOf(Items.ANDESITE)
                || s.isOf(Items.ROTTEN_FLESH)
                || s.isOf(Items.GRAVEL);
    }
}
