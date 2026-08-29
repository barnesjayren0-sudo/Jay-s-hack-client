package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.text.Text;

/** Disconnect when HP drops below threshold — classic anarchy safety. */
public class AutoLog extends Module {

    public final NumberSetting hp = new NumberSetting("HP", "Log below this health", 6.0, 1.0, 20.0, 0.5);

    public AutoLog() {
        super("AutoLog", "Disconnect when low HP", Category.ANARCHY);
        addSetting(hp);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (health <= hp.getFloat()) {
            mc.player.sendMessage(Text.literal("§8[§cAutoLog§8] §fHP " + String.format("%.1f", health)), false);
            mc.getNetworkHandler().getConnection().disconnect(
                    Text.literal("Jay AutoLog · HP " + String.format("%.1f", health)));
            setEnabled(false);
        }
    }
}
