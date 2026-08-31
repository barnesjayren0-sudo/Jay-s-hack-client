package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Export / import full client config as Base64 (clipboard-friendly).
 */
public final class ConfigIO {

    private ConfigIO() {}

    private static Path configPath() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("jayhackclient.txt");
    }

    /** Save current state then return Base64 payload. */
    public static String exportToString() {
        try {
            if (JayHackClient.configManager != null) {
                JayHackClient.configManager.save();
            }
            Path p = configPath();
            if (!Files.exists(p)) {
                Notifications.warn("Config", "Nothing to export");
                return "";
            }
            byte[] raw = Files.readAllBytes(p);
            String b64 = Base64.getEncoder().encodeToString(raw);
            Notifications.success("Config", "Exported (" + raw.length + " bytes)");
            return "JAYCFG1:" + b64;
        } catch (Exception e) {
            Notifications.error("Config", "Export failed");
            return "";
        }
    }

    public static void exportToClipboard() {
        String s = exportToString();
        if (s.isEmpty()) return;
        try {
            MinecraftClient.getInstance().keyboard.setClipboard(s);
            Notifications.success("Config", "Copied to clipboard");
        } catch (Exception e) {
            Notifications.warn("Config", "Clipboard unavailable — use .jay export");
        }
    }

    public static boolean importFromString(String payload) {
        if (payload == null || payload.isBlank()) {
            Notifications.warn("Config", "Empty import");
            return false;
        }
        try {
            String data = payload.trim();
            if (data.startsWith("JAYCFG1:")) data = data.substring(8);
            // Also accept raw file text
            byte[] bytes;
            try {
                bytes = Base64.getDecoder().decode(data.replaceAll("\\s", ""));
            } catch (IllegalArgumentException ex) {
                bytes = payload.getBytes(StandardCharsets.UTF_8);
            }
            Path p = configPath();
            Files.createDirectories(p.getParent());
            Files.write(p, bytes);
            if (JayHackClient.configManager != null) {
                JayHackClient.configManager.load();
            }
            Notifications.success("Config", "Imported");
            return true;
        } catch (Exception e) {
            Notifications.error("Config", "Import failed");
            return false;
        }
    }

    public static void importFromClipboard() {
        try {
            String clip = MinecraftClient.getInstance().keyboard.getClipboard();
            importFromString(clip);
        } catch (Exception e) {
            Notifications.error("Config", "Clipboard read failed");
        }
    }
}
