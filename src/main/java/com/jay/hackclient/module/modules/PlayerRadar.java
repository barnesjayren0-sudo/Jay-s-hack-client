package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Mobile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class PlayerRadar extends Module {

    private long last;

    public PlayerRadar() {
        super("PlayerRadar", "Lists nearby players", Category.WORLD);
    }

    @Override
    public void onTick() {
        // Manual report via .jay radar — don't spam every tick on phones
    }

    public void report(boolean force) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (!force && now - last < 3000) return;
        last = now;

        int count = 0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (AntiBot.isBot(p)) continue;
            count++;
            double d = mc.player.distanceTo(p);
            mc.player.sendMessage(Text.literal(
                    String.format("§8[§bJay§8] §f%s §7%.0fm", p.getName().getString(), d)), false);
            if (Mobile.isSmallScreen() && count >= 5) {
                mc.player.sendMessage(Text.literal("§8[§bJay§8] §7…truncated"), false);
                break;
            }
        }
        if (count == 0) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] §7No players nearby"), false);
        }
    }
}
