package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.gui.GuiLayout;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.module.modules.Reach;
import com.jay.hackclient.module.modules.Waypoints;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Persists ClientSettings + module state + settings + waypoints. */
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
            sb.append("# Jay Client config v1.42\n");
            writeClient(sb);

            if (JayHackClient.moduleManager != null) {
                for (Module m : JayHackClient.moduleManager.getModules()) {
                    sb.append("mod.").append(m.getName()).append('=').append(m.isEnabled()).append('\n');
                    sb.append("key.").append(m.getName()).append('=').append(m.getKeyBind()).append('\n');
                    sb.append("drawn.").append(m.getName()).append('=').append(m.isDrawn()).append('\n');
                    sb.append("keymode.").append(m.getName()).append('=').append(m.getKeyMode().name()).append('\n');
                    sb.append("chatfb.").append(m.getName()).append('=').append(m.isChatFeedback()).append('\n');
                    for (Setting s : m.getSettings()) {
                        String key = "set." + m.getName() + "." + s.getName().replace(' ', '_');
                        if (s instanceof NumberSetting n) {
                            sb.append(key).append('=').append(n.get()).append('\n');
                        } else if (s instanceof BoolSetting b) {
                            sb.append(key).append('=').append(b.get()).append('\n');
                        } else if (s instanceof ModeSetting md) {
                            sb.append(key).append('=').append(md.get()).append('\n');
                        }
                    }
                }
            }
            if (JayHackClient.friendManager != null) {
                for (String f : JayHackClient.friendManager.getFriends()) {
                    sb.append("friend.").append(f).append("=true\n");
                }
            }
            for (String fav : ClientSettings.favorites) {
                sb.append("fav.").append(fav).append("=true\n");
            }
            Waypoints.writeConfig(sb);
            GuiLayout.ensureDefaults();
            for (var e : GuiLayout.POS.entrySet()) {
                float[] xy = e.getValue();
                if (xy == null || xy.length < 2) continue;
                sb.append("panel.").append(e.getKey().name()).append('=')
                        .append(xy[0]).append(',').append(xy[1]).append('\n');
            }
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            System.err.println("[Jay] config save failed: " + e.getMessage());
        }
    }

    private void writeClient(StringBuilder sb) {
        try {
            sb.append("aimMode=").append(ClientSettings.aimMode).append('\n');
            sb.append("targetPriority=").append(ClientSettings.targetPriority).append('\n');
            sb.append("aimRange=").append(ClientSettings.aimRange).append('\n');
            sb.append("aimFov=").append(ClientSettings.aimFov).append('\n');
            sb.append("lastProfile=").append(ClientSettings.lastProfile).append('\n');
            sb.append("mode=").append(ClientSettings.mode).append('\n');
        } catch (Throwable ignored) {}
    }

    public void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            for (String line : Files.readAllLines(p)) {
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                int eq = line.indexOf('=');
                String k = line.substring(0, eq);
                String v = line.substring(eq + 1);
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
                    } else if (k.startsWith("wp.")) {
                        try {
                            String name = k.substring(3);
                            String[] xyz = v.split(",");
                            if (xyz.length >= 3) {
                                Waypoints.loadFromConfig(name,
                                    Integer.parseInt(xyz[0].trim()),
                                    Integer.parseInt(xyz[1].trim()),
                                    Integer.parseInt(xyz[2].trim()));
                            }
                        } catch (Exception ignored) {}
                    } else if (k.startsWith("friend.") && JayHackClient.friendManager != null) {
                        JayHackClient.friendManager.add(k.substring(7));
                    } else if (k.startsWith("fav.")) {
                        ClientSettings.favorites.add(k.substring(4));
                    } else if (k.startsWith("panel.")) {
                        try {
                            Module.Category cat = Module.Category.valueOf(k.substring(6));
                            String[] parts = v.split(",");
                            if (parts.length >= 2) {
                                GuiLayout.set(cat, Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
                            }
                        } catch (Exception ignored) {}
                    } else {
                        readClientKey(k, v);
                    }
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            System.err.println("[Jay] config load failed: " + e.getMessage());
        }
    }

    private void applySetting(String rest, String v) {
        int dot = rest.indexOf('.');
        if (dot < 0 || JayHackClient.moduleManager == null) return;
        String modName = rest.substring(0, dot);
        String setName = rest.substring(dot + 1).replace('_', ' ');
        Module m = JayHackClient.moduleManager.getModuleByName(modName);
        if (m == null) return;
        for (Setting s : m.getSettings()) {
            if (!s.getName().equalsIgnoreCase(setName)) continue;
            if (s instanceof NumberSetting n) {
                try { n.set(Double.parseDouble(v)); } catch (Exception ignored) {}
            } else if (s instanceof BoolSetting b) {
                b.set(Boolean.parseBoolean(v));
            } else if (s instanceof ModeSetting md) {
                md.set(v);
            }
            return;
        }
    }

    private void readClientKey(String k, String v) {
        try {
            if (k.equals("aimMode")) ClientSettings.aimMode = v;
            else if (k.equals("targetPriority")) ClientSettings.targetPriority = v;
            else if (k.equals("aimRange")) ClientSettings.aimRange = Double.parseDouble(v);
            else if (k.equals("aimFov")) ClientSettings.aimFov = Float.parseFloat(v);
            else if (k.equals("lastProfile")) ClientSettings.lastProfile = v;
            else if (k.equals("mode")) ClientSettings.mode = v;
        } catch (Throwable ignored) {}
    }

    public void resetDefaults() {
        try { Files.deleteIfExists(path()); } catch (Exception ignored) {}
        try { ClientSettings.applyDualConfig(); } catch (Throwable ignored) {}
    }
}
