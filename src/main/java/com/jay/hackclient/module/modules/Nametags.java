package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;

/** Extra nametag info — range + HP / distance toggles. */
public class Nametags extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Show within", 64, 16, 128, 4);
    public final BoolSetting health = new BoolSetting("Health", "Show HP", true);
    public final BoolSetting distance = new BoolSetting("Distance", "Show meters", true);
    public final BoolSetting playersOnly = new BoolSetting("PlayersOnly", "Players only", true);
    public final NumberSetting colorR = new NumberSetting("ColorR", "Red", 255, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Green", 255, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Blue", 255, 0, 255, 1);

    public Nametags() {
        super("Nametags", "Enhanced player nametags", Category.RENDER);
        addSetting(range);
        addSetting(health);
        addSetting(distance);
        addSetting(playersOnly);
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

    /** Used by HudRenderer / WorldEspRenderer for on-screen tags. */
    public static String formatTag(PlayerEntity p, PlayerEntity self) {
        Module mod = JayHackClient.moduleManager != null
                ? JayHackClient.moduleManager.getModuleByName("Nametags") : null;
        if (!(mod instanceof Nametags nt) || !mod.isEnabled()) return p.getName().getString();

        StringBuilder sb = new StringBuilder(p.getName().getString());
        if (nt.health.get()) {
            float hp = p.getHealth() + p.getAbsorptionAmount();
            sb.append(" §c").append(String.format("%.0f", hp));
        }
        if (nt.distance.get() && self != null) {
            sb.append(" §7").append(String.format("%.0fm", self.distanceTo(p)));
        }
        if (JayHackClient.friendManager != null
                && JayHackClient.friendManager.isFriend(p.getName().getString())) {
            sb.append(" §a★");
        }
        return sb.toString();
    }

    @Override
    public void onTick() {
        // Rendering handled in HudRenderer.drawNametags
    }
}
