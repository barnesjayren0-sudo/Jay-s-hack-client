package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public final NumberSetting health = new NumberSetting("Health", "Swap below HP (0=always)", 0, 0, 20, 0.5);
    public final BoolSetting soft = new BoolSetting("Soft", "Skip while in GUI", true);
    private int cooldown;
    public AutoTotem() {
        super("AutoTotem", "Auto equip totem in offhand", Category.COMBAT);
        addSetting(health); addSetting(soft);
    }
    @Override public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (cooldown > 0) { cooldown--; return; }
        if (soft.get() && mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen)) return;
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) { setTag("OK"); return; }
        if (health.get() > 0 && mc.player.getHealth() > health.get()) return;
        int slot = -1;
        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) { slot = i; break; }
        }
        if (slot < 0) { setTag("none"); return; }
        int screenSlot = slot < 9 ? slot + 36 : slot;
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, screenSlot, 40, SlotActionType.SWAP, mc.player);
        cooldown = 3; setTag("swap");
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
