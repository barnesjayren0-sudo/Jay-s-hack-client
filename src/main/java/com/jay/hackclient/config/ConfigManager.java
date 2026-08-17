package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final Path file;

    public ConfigManager() {
        Path dir = FabricLoader.getInstance().getConfigDir().resolve("jayhackclient");
        try {
            Files.createDirectories(dir);
        } catch (IOException ignored) {}
        this.file = dir.resolve("config.txt");
    }

    public void save() {
        if (JayHackClient.moduleManager == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("# Jay's Hack Client config\n");
        for (Module m : JayHackClient.moduleManager.getModules()) {
            sb.append("module:").append(m.getName()).append("=").append(m.isEnabled()).append("\n");
        }
        if (JayHackClient.friendManager != null) {
            for (String f : JayHackClient.friendManager.getFriends()) {
                sb.append("friend:").append(f).append("\n");
            }
        }
        try {
            Files.writeString(file, sb.toString());
        } catch (IOException e) {
            System.err.println("[JayHack] Config save failed: " + e.getMessage());
        }
    }

    public void load() {
        if (!Files.exists(file) || JayHackClient.moduleManager == null) return;
        try {
            List<String> lines = Files.readAllLines(file);
            Set<String> friends = new HashSet<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                if (line.startsWith("module:")) {
                    String body = line.substring(7);
                    int eq = body.lastIndexOf('=');
                    if (eq <= 0) continue;
                    String name = body.substring(0, eq);
                    boolean on = Boolean.parseBoolean(body.substring(eq + 1));
                    Module m = JayHackClient.moduleManager.getModuleByName(name);
                    if (m != null) m.setEnabled(on);
                } else if (line.startsWith("friend:")) {
                    friends.add(line.substring(7).trim());
                }
            }
            if (JayHackClient.friendManager != null) {
                JayHackClient.friendManager.setAll(friends);
            }
        } catch (IOException e) {
            System.err.println("[JayHack] Config load failed: " + e.getMessage());
        }
    }
}
