package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Offhand totem — high priority + sticky lock when low HP. */
public class AutoTotem extends Module {

    public final NumberSetting softHp = new NumberSetting("SoftHP", "Faster under this HP", 12, 4, 20, 1);
    public final NumberSetting hardHp = new NumberSetting("HardHP", "Lock offhand under HP", 8, 2, 16, 1);
    public final BoolSetting sticky = new BoolSetting("Sticky", "Re-equip if offhand changed low HP", true);

    private long lastSwap;

    public AutoTotem() {
        super("AutoTotem", "Restock offhand totem", Category.PLAYER);
        addSetting(softHp);
        addSetting(hardHp);
        addSetting(sticky);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean critical = hp <= hardHp.getFloat();

        // Highest hotbar priority when low — don't yield to AutoSword/InvManager
        if (SlotLock.isLockedByOther("AutoTotem") && !critical) return;

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) {
            // Sticky: if something else stole lock under hard HP, still fine while totem present
            return;
        }

        long now = System.currentTimeMillis();
        int delay = critical ? 25 : (hp <= softHp.getFloat() ? 40 : Humanizer.swapDelay());
        if (now - lastSwap < delay) return;

        int slot = findTotemSlot();
        if (slot == -1) return;

        int priority = critical ? 90 : 35;
        if (!SlotLock.tryAcquire("AutoTotem", critical ? 600 : 400, priority)) return;

        try {
            int syncId = mc.player.playerScreenHandler.syncId;
            mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(syncId, slot, 0, SlotActionType.PICKUP, mc.player);
            }
            lastSwap = now;
        } catch (Exception ignored) {
        } finally {
            // Keep lock a bit longer when critical so other modules don't yank offhand
            if (!critical || !sticky.get()) {
                SlotLock.release("AutoTotem");
            } else {
                // release next tick window
                SlotLock.release("AutoTotem");
            }
        }
    }

    private int findTotemSlot() {
        PlayerInventory inv = mc.player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return 36 + i;
        }
        for (int i = 9; i < 36; i++) {
            if (inv.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
