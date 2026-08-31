package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;

/** Enhanced player nametags — HP bar + distance, fixed projection via WorldEspRenderer. */
public class Nametags extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Show within", 64, 16, 128, 4);
    public final BoolSetting health = new BoolSetting("Health", "Show HP bar", true);
    public final BoolSetting distance = new BoolSetting("Distance", "Show meters", true);
    public final BoolSetting armor = new BoolSetting("Armor", "Show armor points", false);
    public final NumberSetting colorR = new NumberSetting("ColorR", "Accent red", 61, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Accent green", 220, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Accent blue", 255, 0, 255, 1);

    public Nametags() {
        super("Nametags", "Screen-space player tags (fixed projection)", Category.RENDER);
        addSetting(range);
        addSetting(health);
        addSetting(distance);
        addSetting(armor);
        addSetting(colorR);
        addSetting(colorG);
        addSetting(colorB);
    }

    public int colorArgb() {
        return 0xFF000000
                | ((colorR.getInt() & 255) << 16)
                | ((colorG.getInt() & 255) << 8)
                | (colorB.getInt() & 255);
    }

    public static String formatTag(PlayerEntity p, PlayerEntity self) {
        Module mod = JayHackClient.moduleManager != null
                ? JayHackClient.moduleManager.getModuleByName("Nametags") : null;
        if (!(mod instanceof Nametags nt) || !mod.isEnabled()) return p.getName().getString();

        StringBuilder sb = new StringBuilder();
        boolean friend = JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(p.getName().getString());
        if (friend) sb.append("§a");
        else sb.append("§f");
        sb.append(p.getName().getString());

        if (nt.health.get()) {
            float hp = p.getHealth() + p.getAbsorptionAmount();
            String hpCol = hp > 14 ? "§a" : (hp > 8 ? "§e" : "§c");
            sb.append(' ').append(hpCol).append(String.format("%.0f", hp));
        }
        if (nt.armor.get()) {
            try {
                sb.append(" §7A").append(p.getArmor());
            } catch (Throwable ignored) {}
        }
        if (nt.distance.get() && self != null) {
            sb.append(" §8").append(String.format("%.0fm", self.distanceTo(p)));
        }
        if (friend) sb.append(" §a★");
        return sb.toString();
    }

    @Override
    public void onTick() {
        // Drawn in WorldEspRenderer.drawHudOverlay
    }
}
