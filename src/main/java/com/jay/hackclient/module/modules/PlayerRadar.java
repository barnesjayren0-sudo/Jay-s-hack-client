package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PlayerRadar extends Module {

    private long lastReport = 0;

    public PlayerRadar() {
        super("PlayerRadar", "Lists nearby players with distance", Category.WORLD);
    }

    @Override
    public void onTick() {
        // Passive — use .jay radar for manual dump; light auto every 8s
        if (mc.world == null || mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastReport < 8000) return;
        lastReport = now;
        report(false);
    }

    public void report(boolean force) {
        if (mc.world == null || mc.player == null) return;

        int count = 0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.distanceTo(p);
            if (d > 128) continue;

            boolean friend = JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString());
            String tag = friend ? "§a[F] " : "§c";

            mc.player.sendMessage(Text.literal(
                    String.format("§8[§3Radar§8] %s%s §7- §f%.1fm",
                            tag, p.getName().getString(), d)
            ), false);
            count++;
            if (count >= 12) break;
        }

        if (force && count == 0) {
            mc.player.sendMessage(Text.literal("§8[§3Radar§8] §7No players in range"), false);
        }
    }
}
