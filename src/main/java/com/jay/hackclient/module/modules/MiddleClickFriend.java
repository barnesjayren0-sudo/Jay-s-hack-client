package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/** Middle-click a player to add/remove friend. */
public class MiddleClickFriend extends Module {

    private boolean wasDown;

    public MiddleClickFriend() {
        super("MiddleClickFriend", "Middle-click player = friend toggle", Category.MISC);
        // on by default for QoL
        setEnabled(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getWindow() == null) return;
        if (mc.currentScreen != null) {
            wasDown = false;
            return;
        }

        long handle = mc.getWindow().getHandle();
        boolean down = GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        if (down && !wasDown) {
            if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
                var e = ((EntityHitResult) mc.crosshairTarget).getEntity();
                if (e instanceof PlayerEntity p && p != mc.player) {
                    String name = p.getName().getString();
                    if (JayHackClient.friendManager == null) return;
                    if (JayHackClient.friendManager.isFriend(name)) {
                        JayHackClient.friendManager.remove(name);
                        msg("§c- friend " + name);
                    } else {
                        JayHackClient.friendManager.add(name);
                        msg("§a+ friend " + name);
                    }
                    if (JayHackClient.configManager != null) {
                        try { JayHackClient.configManager.save(); } catch (Exception ignored) {}
                    }
                }
            }
        }
        wasDown = down;
    }

    private void msg(String s) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
        }
    }
}
