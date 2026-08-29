package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/** Throw ender pearl on middle mouse click. */
public class MiddleClickPearl extends Module {

    private boolean wasDown;

    public MiddleClickPearl() {
        super("MiddleClickPearl", "Middle-click to pearl", Category.ANARCHY);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.getWindow() == null) return;
        if (mc.currentScreen != null) return;

        long handle = mc.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (down && !wasDown) {
            int pearl = -1;
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) {
                    pearl = i;
                    break;
                }
            }
            if (pearl >= 0 && mc.interactionManager != null) {
                int prev = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(pearl);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                mc.player.swingHand(Hand.MAIN_HAND);
                mc.player.getInventory().setSelectedSlot(prev);
            }
        }
        wasDown = down;
    }
}
