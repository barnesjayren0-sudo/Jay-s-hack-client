package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Inventory rules: preferred hotbar slots for totem / gap / pot + junk drop.
 */
public class InvManager extends Module {

    public final NumberSetting totemSlot = new NumberSetting("TotemSlot", "Hotbar 0-8 preferred", 8, 0, 8, 1);
    public final NumberSetting gapSlot = new NumberSetting("GapSlot", "Hotbar gap slot", 1, 0, 8, 1);
    public final NumberSetting potSlot = new NumberSetting("PotSlot", "Hotbar pot slot", 2, 0, 8, 1);
    public final BoolSetting dropJunk = new BoolSetting("DropJunk", "Drop dirt/gravel/etc", true);
    public final BoolSetting organize = new BoolSetting("Organize", "Move totem/gap to slots", true);
    public final NumberSetting delay = new NumberSetting("Delay", "Action ms", 220, 100, 500, 10);

    private long last;

    public InvManager() {
        super("InvManager", "Hotbar rules + junk drop", Category.PLAYER);
        addSetting(totemSlot);
        addSetting(gapSlot);
        addSetting(potSlot);
        addSetting(dropJunk);
        addSetting(organize);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("InvManager")) return;

        long now = System.currentTimeMillis();
        if (now - last < delay.getInt()) return;

        if (organize.get()) {
            if (ensureHotbar(Items.TOTEM_OF_UNDYING, totemSlot.getInt())) {
                last = now;
                return;
            }
            if (ensureHotbar(Items.GOLDEN_APPLE, gapSlot.getInt())
                    || ensureHotbar(Items.ENCHANTED_GOLDEN_APPLE, gapSlot.getInt())) {
                last = now;
                return;
            }
            // pots: splash / drinkable healing-ish
            if (ensurePot(potSlot.getInt())) {
                last = now;
                return;
            }
        }

        if (dropJunk.get()) {
            for (int i = 9; i < 36; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (s.isEmpty() || !isJunk(s)) continue;
                try {
                    if (!SlotLock.tryAcquire("InvManager", 200, 5)) return;
                    int sync = mc.player.playerScreenHandler.syncId;
                    mc.interactionManager.clickSlot(sync, i, 1, SlotActionType.THROW, mc.player);
                    last = now;
                    SlotLock.release("InvManager");
                    return;
                } catch (Exception ignored) {
                    SlotLock.release("InvManager");
                }
            }
        }
    }

    private boolean ensureHotbar(net.minecraft.item.Item item, int hotbarIndex) {
        int invIndex = hotbarIndex; // 0-8
        ItemStack cur = mc.player.getInventory().getStack(invIndex);
        if (cur.isOf(item)) return false;

        // Find item in inventory
        int found = -1;
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getStack(i).isOf(item)) {
                found = i;
                break;
            }
        }
        if (found < 0 || found == invIndex) return false;

        try {
            if (!SlotLock.tryAcquire("InvManager", 250, 5)) return false;
            int sync = mc.player.playerScreenHandler.syncId;
            int from = found < 9 ? 36 + found : found;
            int to = 36 + invIndex;
            mc.interactionManager.clickSlot(sync, from, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(sync, to, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(sync, from, 0, SlotActionType.PICKUP, mc.player);
            }
            SlotLock.release("InvManager");
            return true;
        } catch (Exception e) {
            SlotLock.release("InvManager");
            return false;
        }
    }

    private boolean ensurePot(int hotbarIndex) {
        ItemStack cur = mc.player.getInventory().getStack(hotbarIndex);
        if (isPot(cur)) return false;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!isPot(s)) continue;
            if (i == hotbarIndex) return false;
            try {
                if (!SlotLock.tryAcquire("InvManager", 250, 5)) return false;
                int sync = mc.player.playerScreenHandler.syncId;
                int from = i < 9 ? 36 + i : i;
                int to = 36 + hotbarIndex;
                mc.interactionManager.clickSlot(sync, from, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(sync, to, 0, SlotActionType.PICKUP, mc.player);
                if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    mc.interactionManager.clickSlot(sync, from, 0, SlotActionType.PICKUP, mc.player);
                }
                SlotLock.release("InvManager");
                return true;
            } catch (Exception e) {
                SlotLock.release("InvManager");
                return false;
            }
        }
        return false;
    }

    private boolean isPot(ItemStack s) {
        if (s.isEmpty()) return false;
        return s.isOf(Items.SPLASH_POTION) || s.isOf(Items.POTION)
                || s.isOf(Items.LINGERING_POTION);
    }

    private boolean isJunk(ItemStack s) {
        return s.isOf(Items.DIRT) || s.isOf(Items.GRAVEL) || s.isOf(Items.NETHERRACK)
                || s.isOf(Items.COBBLESTONE) || s.isOf(Items.ROTTEN_FLESH)
                || s.isOf(Items.POISONOUS_POTATO) || s.isOf(Items.ANDESITE)
                || s.isOf(Items.DIORITE) || s.isOf(Items.GRANITE);
    }
}
