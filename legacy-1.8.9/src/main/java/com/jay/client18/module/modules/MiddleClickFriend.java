package com.jay.client18.module.modules;

import com.jay.client18.JayClient18;
import com.jay.client18.module.Module;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;

public class MiddleClickFriend extends Module {

    private boolean wasDown;

    public MiddleClickFriend() {
        super("MiddleClickFriend", "Middle click add friend", Category.MISC);
        setEnabled(true);
    }

    @Override
    public void onTick() {
        boolean down = Mouse.isButtonDown(2);
        if (down && !wasDown && mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer) mc.objectMouseOver.entityHit;
            if (JayClient18.moduleManager.isFriend(p.getName())) {
                JayClient18.moduleManager.removeFriend(p.getName());
                JayClient18.msg("§c- friend " + p.getName());
            } else {
                JayClient18.moduleManager.addFriend(p.getName());
                JayClient18.msg("§a+ friend " + p.getName());
            }
            JayClient18.configManager.save();
        }
        wasDown = down;
    }
}
