package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

/**
 * ESP — glow + 2D boxes (boxes drawn in WorldEspRenderer; glow is optional helper).
 */
public class ESP extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Max distance", 64, 16, 128, 4);
    public final BoolSetting players = new BoolSetting("Players", "Highlight players", true);
    public final BoolSetting hostiles = new BoolSetting("Hostiles", "Hostile mobs", false);
    public final BoolSetting passives = new BoolSetting("Passives", "Passive mobs", false);
    public final BoolSetting friends = new BoolSetting("Friends", "Also highlight friends", true);
    public final BoolSetting glow = new BoolSetting("Glow", "Vanilla glow outline", true);
    public final NumberSetting colorR = new NumberSetting("ColorR", "Red 0-255", 61, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Green 0-255", 220, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Blue 0-255", 255, 0, 255, 1);

    public ESP() {
        super("ESP", "2D boxes + optional glow", Category.RENDER);
        setKeyBind(GLFW.GLFW_KEY_X);
        addSetting(range);
        addSetting(players);
        addSetting(hostiles);
        addSetting(passives);
        addSetting(friends);
        addSetting(glow);
        addSetting(colorR);
        addSetting(colorG);
        addSetting(colorB);
    }

    public int colorArgb() {
        int r = colorR.getInt() & 255;
        int g = colorG.getInt() & 255;
        int b = colorB.getInt() & 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        if (!glow.get()) {
            // Still clear glow when disabled so boxes-only mode is clean
            for (Entity e : mc.world.getEntities()) {
                if (e != mc.player) e.setGlowing(false);
            }
            return;
        }

        double max = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (mc.player.distanceTo(e) > max) {
                e.setGlowing(false);
                continue;
            }

            boolean doGlow = false;
            if (e instanceof PlayerEntity p) {
                if (!players.get()) {
                    p.setGlowing(false);
                    continue;
                }
                try {
                    if (AntiBot.isBot(p)) {
                        p.setGlowing(false);
                        continue;
                    }
                } catch (Throwable ignored) {}
                boolean isFriend = JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(p.getName().getString());
                if (isFriend && !friends.get()) {
                    p.setGlowing(false);
                    continue;
                }
                doGlow = true;
            } else if (e instanceof HostileEntity) {
                doGlow = hostiles.get();
            } else if (e instanceof PassiveEntity) {
                doGlow = passives.get();
            }
            e.setGlowing(doGlow);
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        for (Entity e : mc.world.getEntities()) {
            e.setGlowing(false);
        }
    }
}
