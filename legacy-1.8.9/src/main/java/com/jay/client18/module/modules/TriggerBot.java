package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import com.jay.client18.util.Humanizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

/** Crosshair-only hits — randomized delay, weapons only. */
public class TriggerBot extends Module {

    public boolean weaponsOnly = true;
    public double maxRange = 3.2;
    private long lastHit;
    private int nextDelay = 100;

    public TriggerBot() {
        super("TriggerBot", "Silent crosshair hits", Category.COMBAT);
        setKeyBind(Keyboard.KEY_T);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.combatDelay();
        lastHit = 0;
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly && !CombatUtil.isWeapon(mc.thePlayer.getHeldItem())) return;
        if (mc.thePlayer.isUsingItem()) return;
        if (Humanizer.shouldSkipTick()) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;
        Entity e = mop.entityHit;
        if (!(e instanceof EntityPlayer)) return;
        EntityPlayer p = (EntityPlayer) e;
        if (p == mc.thePlayer || p.isDead || p.getHealth() <= 0) return;
        if (CombatUtil.isFriend(p)) return;
        if (mc.thePlayer.getDistanceToEntity(p) > maxRange) return;

        long now = System.currentTimeMillis();
        if (now - lastHit < nextDelay) return;

        if (Humanizer.shouldMiss()) {
            lastHit = now;
            nextDelay = Humanizer.combatDelay();
            return;
        }

        CombatUtil.attack(p);
        lastHit = now;
        nextDelay = Humanizer.combatDelay();
    }
}
