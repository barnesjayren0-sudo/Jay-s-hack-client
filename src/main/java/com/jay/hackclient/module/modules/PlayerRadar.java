package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chat radar for players already in render distance.
 * Does not locate offline / unloaded / global map positions.
 */
public class PlayerRadar extends Module {

    private long lastReport;
    private long lastAuto;
    private final Set<String> known = new HashSet<>();

    public PlayerRadar() {
        super("PlayerRadar", "Chat nearby players (render distance)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        known.clear();
        report(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();

        // Announce when someone NEW enters render distance
        Set<String> nowSeen = new HashSet<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (AntiBot.isBot(p)) continue;
            String name = p.getName().getString();
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(name)) continue;
            nowSeen.add(name);
            if (!known.contains(name) && now - lastReport > 800) {
                double d = mc.player.distanceTo(p);
                mc.player.sendMessage(Text.literal(String.format(
                        "§8[§bRadar§8] §a+ §f%s §7%.0fm §8%s §c%.0f❤",
                        name, d, directionTo(p), p.getHealth() + p.getAbsorptionAmount())), false);
                lastReport = now;
            }
        }
        known.clear();
        known.addAll(nowSeen);

        // Periodic full list while enabled
        if (now - lastAuto < 10000) return;
        lastAuto = now;
        report(false);
    }

    public void report(boolean force) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (!force && now - lastReport < 2000) return;
        lastReport = now;

        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (AntiBot.isBot(p)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;
            list.add(p);
        }

        list.sort(Comparator.comparingDouble(p -> mc.player.distanceTo(p)));

        if (list.isEmpty()) {
            mc.player.sendMessage(Text.literal(
                    "§8[§bRadar§8] §7No players in render distance"), false);
            return;
        }

        mc.player.sendMessage(Text.literal(
                "§8[§bRadar§8] §f" + list.size() + " player(s) nearby:"), false);

        int max = Mobile.isSmallScreen() ? 6 : 12;
        int n = 0;
        for (PlayerEntity p : list) {
            if (n >= max) break;
            double d = mc.player.distanceTo(p);
            int bx = (int) Math.floor(p.getX());
            int by = (int) Math.floor(p.getY());
            int bz = (int) Math.floor(p.getZ());
            mc.player.sendMessage(Text.literal(String.format(
                    "§8[§bRadar§8] §f%s §7%.0fm §8%s §c%.0f❤ §8@ §b%d %d %d",
                    p.getName().getString(), d, directionTo(p),
                    p.getHealth() + p.getAbsorptionAmount(),
                    bx, by, bz)), false);
            n++;
        }
        if (list.size() > max) {
            mc.player.sendMessage(Text.literal(
                    "§8[§bRadar§8] §7…" + (list.size() - max) + " more"), false);
        }
    }

    private String directionTo(PlayerEntity p) {
        double dx = p.getX() - mc.player.getX();
        double dz = p.getZ() - mc.player.getZ();
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float rel = MathHelper.wrapDegrees(yaw - mc.player.getYaw());
        if (rel >= -22.5 && rel < 22.5) return "N";
        if (rel >= 22.5 && rel < 67.5) return "NE";
        if (rel >= 67.5 && rel < 112.5) return "E";
        if (rel >= 112.5 && rel < 157.5) return "SE";
        if (rel >= -67.5 && rel < -22.5) return "NW";
        if (rel >= -112.5 && rel < -67.5) return "W";
        if (rel >= -157.5 && rel < -112.5) return "SW";
        return "S";
    }
}
