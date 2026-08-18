package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** UHC golden head / player head heal when available. */
public class AutoHead extends Module {

    private long last = 0;
    private final float healthThreshold = 12.0f;

    public AutoHead() {
        super("AutoHead", "Eats golden heads when low (UHC)", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.player.isUsingItem()) return;
        if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > healthThreshold) return;

        long now = System.currentTimeMillis();
        if (now - last < MathUtil.randomDelay(900, 1300)) return;

        int slot = findHead();
        if (slot == -1) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        if (!mc.player.isUsingItem()) {
            mc.player.getInventory().setSelectedSlot(prev);
        }
        last = now;
    }

    private int findHead() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            // Golden heads are often player heads with custom name/data on UHC servers
            if (s.isOf(Items.PLAYER_HEAD) || s.isOf(Items.GOLDEN_APPLE)) {
                String name = s.getName().getString().toLowerCase();
                if (name.contains("head") || name.contains("golden") || s.isOf(Items.PLAYER_HEAD)) {
                    return i;
                }
            }
            if (s.contains(DataComponentTypes.FOOD) && nameLooksLikeHead(s)) return i;
        }
        return -1;
    }

    private boolean nameLooksLikeHead(ItemStack s) {
        String n = s.getName().getString().toLowerCase();
        return n.contains("head") || n.contains("ghead");
    }
}
