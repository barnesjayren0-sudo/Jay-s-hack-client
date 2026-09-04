package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Offhand totem — highest slot priority under HardHP. */
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

        if (SlotLock.isLockedByOther("AutoTotem") && !critical) return;

        ItemStack off = mc.player.getOffHandStack();
        if (off.isOf(Items.TOTEM_OF_UNDYING)) {
            if (critical && sticky.get()) {
                SlotLock.tryAcquire("AutoTotem", 600, SlotLock.PRIO_TOTEM_CRIT);
            }
            return;
        }

        long now = System.currentTimeMillis();
        int delay = critical ? 18 : (hp <= softHp.getFloat() ? 32 : Humanizer.swapDelay());
        if (now - lastSwap < delay) return;

        int slot = findTotemSlot();
        if (slot == -1) return;

        int priority = critical ? SlotLock.PRIO_TOTEM_CRIT : SlotLock.PRIO_TOTEM;
        if (!SlotLock.tryAcquire("AutoTotem", critical ? 900 : 400, priority)) return;

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
