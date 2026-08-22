package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * When holding a pearl and pressing use, softly aims at combat target then throws.
 * Keybind default: none (uses use key).
 */
public class PearlAssist extends Module {

    private long lastThrow;

    public PearlAssist() {
        super("PearlAssist", "Aim + throw pearl at target", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)
                && !mc.player.getOffHandStack().isOf(Items.ENDER_PEARL)) return;

        if (!mc.options.useKey.isPressed()) return;
        long now = System.currentTimeMillis();
        if (now - lastThrow < Humanizer.delay(400, 40, 300, 600)) return;

        PlayerEntity target = TargetUtil.findCombatTarget(24, 90);
        if (target != null) {
            // soft look
            double dx = target.getX() - mc.player.getX();
            double dz = target.getZ() - mc.player.getZ();
            double dy = (target.getY() + target.getHeight() * 0.5) - mc.player.getEyeY();
            double horiz = Math.sqrt(dx * dx + dz * dz);
            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, horiz));
            // lead slightly
            pitch = Math.max(-60, Math.min(60, pitch - 8));
            mc.player.setYaw(yaw + Humanizer.aimJitter());
            mc.player.setPitch(pitch + Humanizer.aimJitter() * 0.5f);
        }

        Hand hand = mc.player.getMainHandStack().isOf(Items.ENDER_PEARL) ? Hand.MAIN_HAND : Hand.OFF_HAND;
        try {
            mc.interactionManager.interactItem(mc.player, hand);
            lastThrow = now;
        } catch (Exception ignored) {}
    }
}
