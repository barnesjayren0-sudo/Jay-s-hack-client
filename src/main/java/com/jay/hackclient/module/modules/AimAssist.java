package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.SilentRotations;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

/**
 * Silent AimAssist — does NOT slowly drag the camera (easy for admins to see).
 * Only snaps rotation for the attack frame, then restores view.
 * Default keybind: J
 */
public class AimAssist extends Module {

    private final double range = 3.5;
    private final float fov = 80f; // only assist if roughly on target
    private long lastHit = 0;
    private int nextDelay = 560;

    public AimAssist() {
        super("AimAssist", "Silent aim — no camera drag (key J)", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (Humanizer.shouldSkipTick(5)) return;
        if (Mobile.shouldThrottle()) return;

        PlayerEntity target = findTarget();
        if (target == null) return;

        // Must be in FOV so it isn't 360 silent rage
        if (!SilentRotations.inFov(target, fov)) return;

        long now = System.currentTimeMillis();
        if (now - lastHit < nextDelay) return;

        // Optional: only when already swinging / pressing attack
        boolean attacking = mc.options.attackKey.isPressed() || mc.player.handSwingTicks > 0;
        if (!attacking && !Humanizer.chance(15)) {
            // mostly wait for player to click — more legit
            return;
        }

        if (Humanizer.chance(5)) {
            lastHit = now;
            nextDelay = Humanizer.combatDelay();
            return; // intentional miss
        }

        SilentRotations.silentLookForHit(target, () -> {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        });

        lastHit = now;
        nextDelay = Humanizer.combatDelay();
    }

    private PlayerEntity findTarget() {
        PlayerEntity best = null;
        double closest = range;

        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (AntiBot.isBot(p)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;

            double d = mc.player.distanceTo(p);
            if (d < closest) {
                closest = d;
                best = p;
            }
        }
        return best;
    }
}
