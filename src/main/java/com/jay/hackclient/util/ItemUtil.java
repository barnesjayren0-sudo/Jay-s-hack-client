package com.jay.hackclient.util;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;

public final class ItemUtil {

    private ItemUtil() {}

    public static boolean isSword(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(ItemTags.SWORDS);
    }

    public static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.isIn(ItemTags.AXES);
    }

    public static boolean isSwordOrAxe(ItemStack stack) {
        return isSword(stack) || isAxe(stack);
    }

    public static boolean isArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.isIn(ItemTags.HEAD_ARMOR)
                || stack.isIn(ItemTags.CHEST_ARMOR)
                || stack.isIn(ItemTags.LEG_ARMOR)
                || stack.isIn(ItemTags.FOOT_ARMOR);
    }

    public static int swordTier(ItemStack stack) {
        if (!isSword(stack)) return 0;
        String id = stack.getItem().toString().toLowerCase();
        if (id.contains("netherite")) return 6;
        if (id.contains("diamond")) return 5;
        if (id.contains("iron")) return 4;
        if (id.contains("stone")) return 3;
        if (id.contains("gold")) return 2;
        if (id.contains("wood") || id.contains("wooden")) return 1;
        return 1;
    }
}
