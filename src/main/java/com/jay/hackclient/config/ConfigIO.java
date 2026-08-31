package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Share full config as Base64 (clipboard-friendly for mobile).
 */
public final class ConfigIO {

    private ConfigIO() {}

    private static Path path() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("jayhackclient.txt");
    }

    public static void exportToClipboard() {
        try {
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            Path p = path();
            if (!Files.exists(p)) {
                Notifications.warn("Config", "Nothing to export");
                return;
            }
            byte[] raw = Files.readAllBytes(p);
            String b64 = Base64.getEncoder().encodeToString(raw);
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.keyboard != null) {
                mc.keyboard.setClipboard(b64);
                Notifications.success("Config", "Exported to clipboard (" + raw.length + "b)");
            } else {
                Notifications.warn("Config", "Clipboard unavailable");
            }
        } catch (Exception e) {
            Notifications.error("Config", "Export failed");
        }
    }

    public static void importFromClipboard() {
        try {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.keyboard == null) {
                Notifications.warn("Config", "Clipboard unavailable");
                return;
            }
            String clip = mc.keyboard.getClipboard();
            if (clip == null || clip.isBlank()) {
                Notifications.warn("Config", "Clipboard empty");
                return;
            }
            clip = clip.trim().replaceAll("\\s+", "");
            byte[] decoded = Base64.getDecoder().decode(clip);
            String text = new String(decoded, StandardCharsets.UTF_8);
            if (!text.contains("mod.") && !text.contains("aimMode")) {
                Notifications.warn("Config", "Does not look like a Jay config");
                return;
            }
            Path p = path();
            Files.createDirectories(p.getParent());
            Files.writeString(p, text);
            if (JayHackClient.configManager != null) JayHackClient.configManager.load();
            Notifications.success("Config", "Imported + applied");
        } catch (Exception e) {
            Notifications.error("Config", "Import failed (need Base64 export)");
        }
    }
}
