package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.gui.GuiLayout;
import com.jay.hackclient.gui.ThemeEngine;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Waypoints;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.render.HudLayout;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Atomic save of client settings, modules, theme, HUD layout, waypoints. */
public class ConfigManager {

    private Path path() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("jayhackclient.txt");
    }

    public void save() {
        try {
            Path p = path();
            Files.createDirectories(p.getParent());
            StringBuilder sb = new StringBuilder();
            sb.append("# Jay Client config v1.48.1\n");
            writeClient(sb);
            try {
                sb.append("theme=").append(ThemeEngine.current.name()).append('\n');
                sb.append("themeOpacity=").append(ThemeEngine.bgOpacity).append('\n');
                sb.append("firstLaunchDone=").append(ClientSettings.firstLaunchDone).append('\n');
            } catch (Throwable ignored) {}
            HudLayout.writeConfig(sb);

            if (JayHackClient.moduleManager != null) {
                for (Module m : JayHackClient.moduleManager.getModules()) {
                    sb.append("mod.").append(m.getName()).append('=').append(m.isEnabled()).append('\n');
                    sb.append("key.").append(m.getName()).append('=').append(m.getKeyBind()).append('\n');
                    sb.append("drawn.").append(m.getName()).append('=').append(m.isDrawn()).append('\n');
                    sb.append("keymode.").append(m.getName()).append('=').append(m.getKeyMode().name()).append('\n');
                    sb.append("chatfb.").append(m.getName()).append('=').append(m.isChatFeedback()).append('\n');
                    for (Setting s : m.getSettings()) {
                        String key = "set." + m.getName() + "." + s.getName().replace(' ', '_');
                        if (s instanceof BoolSetting b) sb.append(key).append('=').append(b.get()).append('\n');
                        else if (s instanceof NumberSetting n) sb.append(key).append('=').append(n.get()).append('\n');
                        else if (s instanceof ModeSetting md) sb.append(key).append('=').append(md.get()).append('\n');
                    }
                }
            }

            for (String fav : ClientSettings.favorites) {
                sb.append("fav.").append(fav).append("=true\n");
            }

            try { GuiLayout.writeConfig(sb); } catch (Throwable ignored) {}
            try { Waypoints.writeConfig(sb); } catch (Throwable ignored) {}

            Path tmp = p.resolveSibling(p.getFileName() + ".tmp");
            Files.writeString(tmp, sb.toString());
            Files.move(tmp, p, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception e) {
            System.err.println("[Jay] config save failed: " + e.getMessage());
        }
    }

    public void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            ClientSettings.favorites.clear();
            for (String line : Files.readAllLines(p)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq <= 0) continue;
                String k = line.substring(0, eq).trim();
                String v = line.substring(eq + 1).trim();
                try {
                    if (k.startsWith("mod.") && JayHackClient.moduleManager != null) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                        if (m != null) m.setEnabled(Boolean.parseBoolean(v));
                    } else if (k.startsWith("key.") && JayHackClient.moduleManager != null) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                        if (m != null) m.setKeyBind(Integer.parseInt(v));
                    } else if (k.startsWith("drawn.") && JayHackClient.moduleManager != null) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(6));
                        if (m != null) m.setDrawn(Boolean.parseBoolean(v));
                    } else if (k.startsWith("keymode.") && JayHackClient.moduleManager != null) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(8));
                        if (m != null) {
                            try { m.setKeyMode(Module.KeyMode.valueOf(v)); } catch (Exception ignored) {}
                        }
                    } else if (k.startsWith("chatfb.") && JayHackClient.moduleManager != null) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(7));
                        if (m != null) m.setChatFeedback(Boolean.parseBoolean(v));
                    } else if (k.startsWith("set.") && JayHackClient.moduleManager != null) {
                        applySetting(k.substring(4), v);
                    } else if (k.startsWith("fav.")) {
                        ClientSettings.favorites.add(k.substring(4));
                    } else if (k.startsWith("hud.")) {
                        HudLayout.loadLine(k, v);
                    } else if (k.startsWith("panel.") || k.startsWith("gui.")) {
                        try { GuiLayout.loadLine(k, v); } catch (Throwable ignored) {}
                    } else if (k.startsWith("wp.")) {
                        try { Waypoints.loadLine(k, v); } catch (Throwable ignored) {}
                    } else if (k.equals("theme")) {
                        try { ThemeEngine.applyByName(v); } catch (Throwable ignored) {}
                    } else if (k.equals("themeOpacity")) {
                        try { ThemeEngine.bgOpacity = Float.parseFloat(v); } catch (Throwable ignored) {}
                    } else if (k.equals("firstLaunchDone")) {
                        ClientSettings.firstLaunchDone = Boolean.parseBoolean(v);
                    } else {
                        readClientKey(k, v);
                    }
                } catch (Throwable ignored) {}
            }
        } catch (Exception e) {
            System.err.println("[Jay] config load failed: " + e.getMessage());
        }
    }

    private void applySetting(String rest, String v) {
        int dot = rest.indexOf('.');
        if (dot <= 0) return;
        String modName = rest.substring(0, dot);
        String setName = rest.substring(dot + 1).replace('_', ' ');
        Module m = JayHackClient.moduleManager.getModuleByName(modName);
        if (m == null) return;
        for (Setting s : m.getSettings()) {
            if (!s.getName().equalsIgnoreCase(setName)) continue;
            try {
                if (s instanceof BoolSetting b) b.set(Boolean.parseBoolean(v));
                else if (s instanceof NumberSetting n) n.set(Double.parseDouble(v));
                else if (s instanceof ModeSetting md) md.set(v);
            } catch (Throwable ignored) {}
            return;
        }
    }

    private void writeClient(StringBuilder sb) {
        try {
            sb.append("aimMode=").append(ClientSettings.aimMode).append('\n');
            sb.append("targetPriority=").append(ClientSettings.targetPriority).append('\n');
            sb.append("aimRange=").append(ClientSettings.aimRange).append('\n');
            sb.append("aimFov=").append(ClientSettings.aimFov).append('\n');
            sb.append("aimSmooth=").append(ClientSettings.aimSmooth).append('\n');
            sb.append("auraRange=").append(ClientSettings.auraRange).append('\n');
            sb.append("velocityHorizontal=").append(ClientSettings.velocityHorizontal).append('\n');
            sb.append("toggleSounds=").append(ClientSettings.toggleSounds).append('\n');
            sb.append("showActiveCount=").append(ClientSettings.showActiveCount).append('\n');
            sb.append("pingScaleDelays=").append(ClientSettings.pingScaleDelays).append('\n');
            sb.append("hideHudOnScreenshot=").append(ClientSettings.hideHudOnScreenshot).append('\n');
            sb.append("lastProfile=").append(ClientSettings.lastProfile).append('\n');
            sb.append("mode=").append(ClientSettings.mode).append('\n');
            sb.append("guiScale=").append(ClientSettings.guiScale).append('\n');
        } catch (Throwable ignored) {}
    }

    private void readClientKey(String k, String v) {
        try {
            switch (k) {
                case "aimMode" -> ClientSettings.aimMode = v;
                case "targetPriority" -> ClientSettings.targetPriority = v;
                case "aimRange" -> ClientSettings.aimRange = Double.parseDouble(v);
                case "aimFov" -> ClientSettings.aimFov = Float.parseFloat(v);
                case "aimSmooth" -> ClientSettings.aimSmooth = Float.parseFloat(v);
                case "auraRange" -> ClientSettings.auraRange = Double.parseDouble(v);
                case "velocityHorizontal" -> ClientSettings.velocityHorizontal = Double.parseDouble(v);
                case "toggleSounds" -> ClientSettings.toggleSounds = Boolean.parseBoolean(v);
                case "showActiveCount" -> ClientSettings.showActiveCount = Boolean.parseBoolean(v);
                case "pingScaleDelays" -> ClientSettings.pingScaleDelays = Boolean.parseBoolean(v);
                case "hideHudOnScreenshot" -> ClientSettings.hideHudOnScreenshot = Boolean.parseBoolean(v);
                case "lastProfile" -> ClientSettings.lastProfile = v;
                case "mode" -> ClientSettings.mode = v;
                case "guiScale" -> ClientSettings.guiScale = Math.max(0.85f, Math.min(1.25f, Float.parseFloat(v)));
                default -> {}
            }
        } catch (Throwable ignored) {}
    }

    public void resetDefaults() {
        try { Files.deleteIfExists(path()); } catch (Exception ignored) {}
        try { ClientSettings.applyDualConfig(); } catch (Throwable ignored) {}
    }
}
