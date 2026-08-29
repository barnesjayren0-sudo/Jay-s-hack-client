package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.BoolSetting;

/** Prefer critical hits — packet or jump assist. */
public class Criticals extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Crit style", "Jump", "Jump", "Off");
    public final BoolSetting onlyWeapon = new BoolSetting("OnlyWeapon", "Sword/axe only", true);

    public Criticals() {
        super("Criticals", "Help land critical hits", Category.COMBAT);
        addSetting(mode);
        addSetting(onlyWeapon);
    }

    @Override
    public void onEnable() {
        // Share flag with CritAssist path
        com.jay.hackclient.settings.ClientSettings.critTiming = !"Off".equals(mode.get());
    }

    @Override
    public void onDisable() {
        com.jay.hackclient.settings.ClientSettings.critTiming = false;
    }

    @Override
    public void onTick() {
        com.jay.hackclient.settings.ClientSettings.critTiming = isEnabled() && !"Off".equals(mode.get());
    }
}
