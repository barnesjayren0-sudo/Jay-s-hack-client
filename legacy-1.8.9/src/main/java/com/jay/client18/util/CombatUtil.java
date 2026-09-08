package com.jay.client18.util;

import com.jay.client18.JayClient18;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

import java.util.ArrayList;
import java.util.List;

public final class CombatUtil {

    private CombatUtil() {}

    public static boolean isWeapon(ItemStack s) {
        if (s == null) return false;
        return s.getItem() instanceof ItemSword || s.getItem() instanceof ItemAxe;
    }

    public static boolean isFriend(EntityPlayer p) {
        if (p == null) return false;
        return JayClient18.moduleManager != null
                && JayClient18.moduleManager.isFriend(p.getName());
    }

    public static EntityPlayer findTarget(double range, float fov) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return null;

        EntityPlayer best = null;
        double bestScore = Double.MAX_VALUE;

        for (Object o : mc.theWorld.playerEntities) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) o;
            if (p == mc.thePlayer || p.isDead || p.getHealth() <= 0) continue;
            if (isFriend(p)) continue;
            double d = mc.thePlayer.getDistanceToEntity(p);
            if (d > range) continue;
            if (RotationUtil.yawDiff(p) > fov * 0.5f) continue;
            // prefer closest
            if (d < bestScore) {
                bestScore = d;
                best = p;
            }
        }
        return best;
    }

    public static List<EntityPlayer> playersInRange(double range) {
        List<EntityPlayer> list = new ArrayList<EntityPlayer>();
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return list;
        for (Object o : mc.theWorld.playerEntities) {
            if (!(o instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) o;
            if (p == mc.thePlayer || p.isDead) continue;
            if (mc.thePlayer.getDistanceToEntity(p) <= range) list.add(p);
        }
        return list;
    }

    public static void attack(Entity e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || e == null) return;
        mc.playerController.attackEntity(mc.thePlayer, e);
        mc.thePlayer.swingItem();
    }
}
