package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Lists players already in render distance (no global locate). */
public class PlayerRadar extends Module {

    private long last;
    private long lastAuto;

    public PlayerRadar() {
        super("PlayerRadar", "Players in render distance", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAuto < 8000) return;
        lastAuto = now;
        report(false);
    }

    public void report(boolean force) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (!force && now - last < 2500) return;
        last = now;

        List<PlayerEntity> list = new ArrayList<>();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive() || p.isSpectator()) continue;
            if (AntiBot.isBot(p)) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;
            list.add(p);
        }

        list.sort(Comparator.comparingDouble(p -> mc.player.distanceTo(p)));

        int max = Mobile.isSmallScreen() ? 6 : 12;
        int count = 0;
        for (PlayerEntity p : list) {
            if (count >= max) break;
            double d = mc.player.distanceTo(p);
            String dir = directionTo(p);
            float hp = p.getHealth() + p.getAbsorptionAmount();
            mc.player.sendMessage(Text.literal(String.format(
                    "§8[§bRadar§8] §f%s §7%.0fm §8%s §c%.0f❤",
                    p.getName().getString(), d, dir, hp)), false);
            count++;
        }

        if (count == 0) {
            mc.player.sendMessage(Text.literal("§8[§bRadar§8] §7No players in render distance"), false);
        } else if (list.size() > max) {
            mc.player.sendMessage(Text.literal("§8[§bRadar§8] §7…" + (list.size() - max) + " more"), false);
        }
    }

    private String directionTo(PlayerEntity p) {
        double dx = p.getX() - mc.player.getX();
        double dz = p.getZ() - mc.player.getZ();
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90f;
        float rel = MathHelper.wrapDegrees(yaw - mc.player.getYaw());
        if (rel >= -22.5 && rel < 22.5) return "↑";
        if (rel >= 22.5 && rel < 67.5) return "↗";
        if (rel >= 67.5 && rel < 112.5) return "→";
        if (rel >= 112.5 && rel < 157.5) return "↘";
        if (rel >= -67.5 && rel < -22.5) return "↖";
        if (rel >= -112.5 && rel < -67.5) return "←";
        if (rel >= -157.5 && rel < -112.5) return "↙";
        return "↓";
    }
}
