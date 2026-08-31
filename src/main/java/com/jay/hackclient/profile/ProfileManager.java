package com.jay.hackclient.profile;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Save / load named profiles under config/jayprofiles/ */
public final class ProfileManager {

    private ProfileManager() {}

    private static Path dir() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("jayprofiles");
    }

    public static List<String> list() {
        try {
            Path d = dir();
            if (!Files.isDirectory(d)) return List.of();
            try (Stream<Path> s = Files.list(d)) {
                return s.filter(p -> p.toString().endsWith(".txt"))
                        .map(p -> p.getFileName().toString().replace(".txt", ""))
                        .sorted()
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            return List.of();
        }
    }

    public static void save(String name) {
        if (name == null || name.isBlank() || JayHackClient.moduleManager == null) return;
        name = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        try {
            Path d = dir();
            Files.createDirectories(d);
            StringBuilder sb = new StringBuilder();
            sb.append("# Jay profile ").append(name).append('\n');
            for (Module m : JayHackClient.moduleManager.getModules()) {
                sb.append("mod.").append(m.getName()).append('=').append(m.isEnabled()).append('\n');
                sb.append("key.").append(m.getName()).append('=').append(m.getKeyBind()).append('\n');
                for (Setting s : m.getSettings()) {
                    String k = "set." + m.getName() + "." + s.getName().replace(' ', '_');
                    if (s instanceof NumberSetting n) sb.append(k).append('=').append(n.get()).append('\n');
                    else if (s instanceof BoolSetting b) sb.append(k).append('=').append(b.get()).append('\n');
                    else if (s instanceof ModeSetting md) sb.append(k).append('=').append(md.get()).append('\n');
                }
            }
            Files.writeString(d.resolve(name + ".txt"), sb.toString());
            Notifications.push("Profile", "Saved " + name);
        } catch (IOException e) {
            Notifications.push("Profile", "Save failed");
        }
    }

    public static void load(String name) {
        if (name == null || JayHackClient.moduleManager == null) return;
        try {
            Path p = dir().resolve(name + ".txt");
            if (!Files.exists(p)) {
                Notifications.push("Profile", "Not found: " + name);
                return;
            }
            for (String line : Files.readAllLines(p)) {
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                int eq = line.indexOf('=');
                String k = line.substring(0, eq);
                String v = line.substring(eq + 1);
                try {
                    if (k.startsWith("mod.")) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                        if (m != null) m.setEnabled(Boolean.parseBoolean(v));
                    } else if (k.startsWith("key.")) {
                        Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                        if (m != null) m.setKeyBind(Integer.parseInt(v));
                    } else if (k.startsWith("set.")) {
                        applySetting(k.substring(4), v);
                    }
                } catch (Exception ignored) {}
            }
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            Notifications.push("Profile", "Loaded " + name);
        } catch (IOException e) {
            Notifications.push("Profile", "Load failed");
        }
    }

    public static void delete(String name) {
        try {
            Files.deleteIfExists(dir().resolve(name + ".txt"));
            Notifications.push("Profile", "Deleted " + name);
        } catch (IOException ignored) {}
    }

    private static void applySetting(String rest, String v) {
        int dot = rest.indexOf('.');
        if (dot < 0) return;
        Module m = JayHackClient.moduleManager.getModuleByName(rest.substring(0, dot));
        if (m == null) return;
        String setName = rest.substring(dot + 1).replace('_', ' ');
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

    public static List<String> suggestNames() {
        List<String> out = new ArrayList<>(list());
        if (out.isEmpty()) {
            out.add("PvP");
            out.add("Hypixel");
            out.add("Survival");
            out.add("Custom");
        }
        return out;
    }
}
