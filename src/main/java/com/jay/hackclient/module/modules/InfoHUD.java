package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;

/** Meteor-style watermark extras — coords / FPS / speed drawn from HudRenderer when enabled. */
public class InfoHUD extends Module {

    public static final BoolSetting coords = new BoolSetting("Coords", "Show XYZ", true);
    public static final BoolSetting fps = new BoolSetting("FPS", "Show FPS", true);
    public static final BoolSetting speed = new BoolSetting("Speed", "Show blocks/s", true);
    public static final BoolSetting ping = new BoolSetting("Ping", "Show latency", true);

    public InfoHUD() {
        super("InfoHUD", "Coords, FPS, speed, ping", Category.RENDER);
        addSetting(coords);
        addSetting(fps);
        addSetting(speed);
        addSetting(ping);
        setEnabled(true);
    }
}
