package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Keep totem / gap / pot slots filled from inventory. */
public class InvManager extends Module {

    public final NumberSetting totemSlot = new NumberSetting("TotemHotbar", "Hotbar index 0-8 (-1 off)", -1, -1, 8, 1);
    public final NumberSetting gapSlot = new NumberSetting("GapHotbar", "Gap hotbar index", 1, -1, 8, 1);
    public final NumberSetting potSlot = new NumberSetting("PotHotbar", "Pot hotbar index", 2, -1, 8, 1);
    public final BoolSetting dropJunk = new BoolSetting("DropJunk", "Drop dirt/cobble stack clutter", false);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between actions", 160, 80, 400, 10);

    private long last;

    public InvManager() {
        super("InvManager", "Totem / gap / pot slot rules", Category.PLAYER);
        addSetting(totemSlot);
        addSetting(gapSlot);
        addSetting(potSlot);
        addSetting(dropJunk);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("InvManager")) return;

        long now = System.currentTimeMillis();
        if (now - last < Math.max(delay.getInt(), Humanizer.swapDelay())) return;

        int t = totemSlot.getInt();
        int g = gapSlot.getInt();
        int p = potSlot.getInt();

        if (t >= 0 && ensureItem(t, Items.TOTEM_OF_UNDYING)) { last = now; return; }
        if (g >= 0 && ensureGap(g)) { last = now; return; }
        if (p >= 0 && ensurePot(p)) { last = now; return; }

        if (dropJunk.get()) {
            for (int i = 0; i < 36; i++) {
                ItemStack s = mc.player.getInventory().getStack(i);
                if (!isJunk(s)) continue;
                try {
                    if (!SlotLock.tryAcquire("InvManager", 200, SlotLock.PRIO_INV)) return;
                    int sync = mc.player.playerScreenHandler.syncId;
                    int slot = i < 9 ? 36 + i : i;
                    mc.interactionManager.clickSlot(sync, slot, 1, SlotActionType.THROW, mc.player);
                    SlotLock.release("InvManager");
                    last = now;
                    return;
                } catch (Exception e) {
                    SlotLock.release("InvManager");
                }
            }
        }
    }

    private boolean ensureItem(int invIndex, net.minecraft.item.Item item) {
        ItemStack cur = mc.player.getInventory().getStack(invIndex);
        if (cur.isOf(item)) return false;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isOf(item)) continue;
            if (i == invIndex) return false;
            return swapSlots(i, invIndex);
        }
        return false;
    }

    private boolean ensureGap(int invIndex) {
        ItemStack cur = mc.player.getInventory().getStack(invIndex);
        if (cur.isOf(Items.GOLDEN_APPLE) || cur.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return false;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!(s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE))) continue;
            if (i == invIndex) return false;
            return swapSlots(i, invIndex);
        }
        return false;
    }

    private boolean ensurePot(int hotbarIndex) {
        ItemStack cur = mc.player.getInventory().getStack(hotbarIndex);
        if (isPot(cur)) return false;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!isPot(s)) continue;
            if (i == hotbarIndex) return false;
            return swapSlots(i, hotbarIndex);
        }
        return false;
    }

    private boolean swapSlots(int fromInv, int toHotbar) {
        try {
            if (!SlotLock.tryAcquire("InvManager", 250, SlotLock.PRIO_INV)) return false;
            int sync = mc.player.playerScreenHandler.syncId;
            int from = fromInv < 9 ? 36 + fromInv : fromInv;
            int to = 36 + toHotbar;
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
