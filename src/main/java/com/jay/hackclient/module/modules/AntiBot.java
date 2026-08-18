package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Filters likely NPC/bots so combat modules ignore them. */
public class AntiBot extends Module {

    private static final Set<UUID> bots = new HashSet<>();

    public AntiBot() {
        super("AntiBot", "Ignore NPC/bot players", Category.COMBAT);
    }

    public static boolean isBot(PlayerEntity p) {
        if (p == null) return true;
        if (bots.contains(p.getUuid())) return true;

        // No tab-list entry → often NPC
        if (MinecraftHolder.mc() != null && MinecraftHolder.mc().getNetworkHandler() != null) {
            PlayerListEntry entry = MinecraftHolder.mc().getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (entry == null) {
                bots.add(p.getUuid());
                return true;
            }
        }

        String name = p.getName().getString();
        if (name.startsWith("CIT-") || name.contains("NPC") || name.contains("Bot")) {
            bots.add(p.getUuid());
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        bots.clear();
    }

    /** Tiny accessor to avoid circular imports in static context */
    private static final class MinecraftHolder {
        static net.minecraft.client.MinecraftClient mc() {
            return net.minecraft.client.MinecraftClient.getInstance();
        }
    }
}
