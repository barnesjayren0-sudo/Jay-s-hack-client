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

/** Player / mob glow ESP with range + filters. */
public class ESP extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Max distance", 64, 16, 128, 4);
    public final BoolSetting players = new BoolSetting("Players", "Highlight players", true);
    public final BoolSetting hostiles = new BoolSetting("Hostiles", "Hostile mobs", false);
    public final BoolSetting passives = new BoolSetting("Passives", "Passive mobs", false);
    public final BoolSetting friends = new BoolSetting("Friends", "Also highlight friends", true);
    /** Color tint hint for HUD markers (ARGB hue not applied to vanilla glow). */
    public final NumberSetting colorR = new NumberSetting("ColorR", "Red 0-255", 61, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Green 0-255", 220, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Blue 0-255", 255, 0, 255, 1);

    public ESP() {
        super("ESP", "Glow players / mobs", Category.RENDER);
        setKeyBind(GLFW.GLFW_KEY_X);
        addSetting(range);
        addSetting(players);
        addSetting(hostiles);
        addSetting(passives);
        addSetting(friends);
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
        double max = range.get();

        for (Entity e : mc.world.getEntities()) {
            if (e == mc.player) continue;
            if (mc.player.distanceTo(e) > max) {
                e.setGlowing(false);
                continue;
            }

            boolean glow = false;
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
                glow = true;
            } else if (e instanceof HostileEntity) {
                glow = hostiles.get();
            } else if (e instanceof PassiveEntity) {
                glow = passives.get();
            }

            e.setGlowing(glow);
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
