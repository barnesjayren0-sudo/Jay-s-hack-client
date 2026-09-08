package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Keyboard;

public class TriggerBot extends Module {

    public boolean weaponsOnly = true;
    public long delayMs = 90;
    private long lastHit;

    public TriggerBot() {
        super("TriggerBot", "Hit on crosshair", Category.COMBAT);
        setKeyBind(Keyboard.KEY_T);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly && !CombatUtil.isWeapon(mc.thePlayer.getHeldItem())) return;

        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY) return;
        Entity e = mop.entityHit;
        if (!(e instanceof EntityPlayer)) return;
        EntityPlayer p = (EntityPlayer) e;
        if (p == mc.thePlayer || p.isDead) return;
        if (CombatUtil.isFriend(p)) return;

        long now = System.currentTimeMillis();
        if (now - lastHit < delayMs) return;

        // 1.8.9: simple timer — wait for swing cooldown-ish
        if (mc.thePlayer.isUsingItem()) return;

        CombatUtil.attack(p);
        lastHit = now;
        delayMs = 80 + (long) (Math.random() * 40);
    }
}
