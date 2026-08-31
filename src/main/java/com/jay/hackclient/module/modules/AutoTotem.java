package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Offhand totem — hard lock under HP threshold so AutoSword/InvManager cannot steal. */
public class AutoTotem extends Module {

    public final NumberSetting softHp = new NumberSetting("SoftHP", "Faster under this HP", 12, 4, 20, 1);
    public final NumberSetting hardHp = new NumberSetting("HardHP", "Lock offhand under HP", 8, 2, 16, 1);
    public final BoolSetting sticky = new BoolSetting("Sticky", "Re-equip if offhand changed while low", true);
    public final BoolSetting onlyInCombat = new BoolSetting("CombatOnly", "Only swap when recently hurt", false);

    private long lastSwap;
    private long lastHurtMs;

    public AutoTotem() {
        super("AutoTotem", "Hard offhand totem lock", Category.PLAYER);
        addSetting(softHp);
        addSetting(hardHp);
        addSetting(sticky);
        addSetting(onlyInCombat);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;

        if (mc.player.hurtTime > 0) lastHurtMs = System.currentTimeMillis();
        if (onlyInCombat.get() && System.currentTimeMillis() - lastHurtMs > 4000) return;

        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        boolean critical = hp <= hardHp.getFloat();

        // Never yield hotbar under hard HP
        if (SlotLock.isLockedByOther("AutoTotem") && !critical) return;

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) {
            if (critical && sticky.get()) {
                // Keep lock so other modules cannot swap offhand
                SlotLock.tryAcquire("AutoTotem", 500, 95);
            }
            return;
        }

        long now = System.currentTimeMillis();
        int delay = critical ? 20 : (hp <= softHp.getFloat() ? 35 : Humanizer.swapDelay());
        if (now - lastSwap < delay) return;

        int slot = findTotemSlot();
        if (slot == -1) return;

        int priority = critical ? 95 : 40;
        if (!SlotLock.tryAcquire("AutoTotem", critical ? 800 : 400, priority)) return;

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
            if (!critical) SlotLock.release("AutoTotem");
        }
    }

    private int findTotemSlot() {
        // hotbar first
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) return i + 36;
        }
        for (int i = 9; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) return i;
        }
        return -1;
    }
}
