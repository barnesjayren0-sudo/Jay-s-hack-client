package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.SlotActionType;

/** Equip best armor by material tier + prefer higher count durability. */
public class AutoArmor extends Module {

    public final NumberSetting delay = new NumberSetting("Delay", "Ms between swaps", 350, 150, 800, 25);

    private long last;

    public AutoArmor() {
        super("AutoArmor", "Equip best armor by tier", Category.PLAYER);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (SlotLock.isLockedByOther("AutoArmor")) return;

        long now = System.currentTimeMillis();
        if (now - last < delay.getInt()) return;

        // Armor slots in player inventory: 36+ armor is different — use equipment
        if (trySlot(EquipmentSlot.HEAD, ItemTags.HEAD_ARMOR)) { last = now; return; }
        if (trySlot(EquipmentSlot.CHEST, ItemTags.CHEST_ARMOR)) { last = now; return; }
        if (trySlot(EquipmentSlot.LEGS, ItemTags.LEG_ARMOR)) { last = now; return; }
        if (trySlot(EquipmentSlot.FEET, ItemTags.FOOT_ARMOR)) { last = now; return; }
    }

    private boolean trySlot(EquipmentSlot slot, net.minecraft.registry.tag.TagKey<net.minecraft.item.Item> tag) {
        ItemStack equipped = mc.player.getEquippedStack(slot);
        int bestInv = -1;
        int bestScore = score(equipped);

        for (int i = 0; i < 36; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !s.isIn(tag)) continue;
            int sc = score(s);
            if (sc > bestScore) {
                bestScore = sc;
                bestInv = i;
            }
        }
        if (bestInv < 0) return false;

        // Click equip: armor slots 5-8 in player screen handler (head=5 ... feet=8)
        int armorSlot = switch (slot) {
            case HEAD -> 5;
            case CHEST -> 6;
            case LEGS -> 7;
            case FEET -> 8;
            default -> -1;
        };
        if (armorSlot < 0) return false;

        try {
            if (!SlotLock.tryAcquire("AutoArmor", 300, 4)) return false;
            int sync = mc.player.playerScreenHandler.syncId;
            int from = bestInv < 9 ? 36 + bestInv : bestInv;
            mc.interactionManager.clickSlot(sync, from, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(sync, armorSlot, 0, SlotActionType.PICKUP, mc.player);
            if (!mc.player.currentScreenHandler.getCursorStack().isEmpty()) {
                mc.interactionManager.clickSlot(sync, from, 0, SlotActionType.PICKUP, mc.player);
            }
            SlotLock.release("AutoArmor");
            return true;
        } catch (Exception e) {
            SlotLock.release("AutoArmor");
            return false;
        }
    }

    private int score(ItemStack s) {
        if (s == null || s.isEmpty()) return -1;
        String id = s.getItem().toString().toLowerCase();
        int tier = 1;
        if (id.contains("netherite")) tier = 6;
        else if (id.contains("diamond")) tier = 5;
        else if (id.contains("iron")) tier = 4;
        else if (id.contains("chain")) tier = 3;
        else if (id.contains("gold")) tier = 2;
        else if (id.contains("leather")) tier = 1;
        int max = s.getMaxDamage();
        int left = max <= 0 ? 100 : (max - s.getDamage());
        return tier * 1000 + left;
    }
}
