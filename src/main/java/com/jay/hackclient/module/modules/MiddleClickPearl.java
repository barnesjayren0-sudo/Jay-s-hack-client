package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class MiddleClickPearl extends Module {
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Slot packets", true);
    private boolean wasPressed;
    public MiddleClickPearl() { super("MiddleClickPearl", "Middle-click throw ender pearl", Category.PLAYER); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.getWindow() == null) return;
        boolean pressed = GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;
        if (!pressed) { wasPressed = false; return; }
        if (wasPressed) return; wasPressed = true;
        int slot = -1;
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(Items.ENDER_PEARL)) { slot = i; break; }
        if (slot < 0 && !mc.player.getOffHandStack().isOf(Items.ENDER_PEARL)) return;
        int prev = mc.player.getInventory().selectedSlot; Hand hand = Hand.MAIN_HAND;
        if (slot >= 0) { if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot; }
        else hand = Hand.OFF_HAND;
        mc.interactionManager.interactItem(mc.player, hand);
        if (slot >= 0) { if (realPackets.get()) RealPackets.selectSlot(prev); else mc.player.getInventory().selectedSlot = prev; }
    }
}
