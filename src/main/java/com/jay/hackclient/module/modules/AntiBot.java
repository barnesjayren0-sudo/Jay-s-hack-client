package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Filters NPC / bots — configurable. */
public class AntiBot extends Module {

    public final BoolSetting noTab = new BoolSetting("NoTab", "No tab-list entry", true);
    public final BoolSetting nameFilter = new BoolSetting("NameFilter", "NPC/Bot/CIT names", true);
    public final BoolSetting shortName = new BoolSetting("ShortName", "Name length < 3", true);
    public final BoolSetting armorStandLike = new BoolSetting("Invisible", "Invisible players", false);

    private static AntiBot INSTANCE;
    private static final Set<UUID> bots = new HashSet<>();

    public AntiBot() {
        super("AntiBot", "Ignore NPC/bot players", Category.COMBAT);
        addSetting(noTab);
        addSetting(nameFilter);
        addSetting(shortName);
        addSetting(armorStandLike);
        INSTANCE = this;
    }

    public static boolean isBot(PlayerEntity p) {
        if (p == null) return true;
        if (bots.contains(p.getUuid())) return true;

        AntiBot self = INSTANCE;
        boolean useNoTab = self == null || !self.isEnabled() || self.noTab.get();
        boolean useName = self == null || !self.isEnabled() || self.nameFilter.get();
        boolean useShort = self != null && self.isEnabled() && self.shortName.get();
        boolean useInvis = self != null && self.isEnabled() && self.armorStandLike.get();

        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (useNoTab && mc != null && mc.getNetworkHandler() != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (entry == null) {
                bots.add(p.getUuid());
                return true;
            }
        }

        String name = p.getName().getString();
        if (useName && (name.startsWith("CIT-") || name.contains("NPC")
                || name.toLowerCase().contains("bot") || name.startsWith("["))) {
            bots.add(p.getUuid());
            return true;
        }
        if (useShort && name.length() < 3) {
            bots.add(p.getUuid());
            return true;
        }
        if (useInvis && p.isInvisible()) {
            return true;
        }
        return false;
    }

    @Override
    public void onDisable() {
        bots.clear();
    }
}
