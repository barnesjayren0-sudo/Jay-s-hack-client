package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/** Export / import full client config as Base64 for sharing. */
public final class ConfigIO {

    private ConfigIO() {}

    private static Path path() {
        return MinecraftClient.getInstance().runDirectory.toPath()
                .resolve("config").resolve("jayhackclient.txt");
    }

    /** Save current config then return Base64 payload (or empty). */
    public static String exportBase64() {
        try {
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            Path p = path();
            if (!Files.exists(p)) return "";
            byte[] raw = Files.readAllBytes(p);
            String b64 = Base64.getEncoder().encodeToString(raw);
            Notifications.success("Config", "Exported (" + raw.length + " bytes)");
            return b64;
        } catch (Exception e) {
            Notifications.error("Config", "Export failed");
            return "";
        }
    }

    public static boolean importBase64(String b64) {
        if (b64 == null || b64.isBlank()) {
            Notifications.warn("Config", "Empty import");
            return false;
        }
        try {
            // strip whitespace / chat wrapping
            String clean = b64.replaceAll("\\s+", "");
            byte[] raw = Base64.getDecoder().decode(clean);
            String text = new String(raw, StandardCharsets.UTF_8);
            if (!text.contains("mod.") && !text.contains("aim")) {
                Notifications.warn("Config", "Doesn't look like a Jay config");
                return false;
            }
            Path p = path();
            Files.createDirectories(p.getParent());
            Files.writeString(p, text);
            if (JayHackClient.configManager != null) JayHackClient.configManager.load();
            Notifications.success("Config", "Imported");
            return true;
        } catch (Exception e) {
            Notifications.error("Config", "Import failed");
            return false;
        }
    }

    /** Copy export to clipboard when executor supports setClipboardString via Minecraft. */
    public static void exportToClipboard() {
        String b64 = exportBase64();
        if (b64.isEmpty()) return;
        try {
            MinecraftClient.getInstance().keyboard.setClipboard(b64);
            Notifications.success("Config", "Copied to clipboard");
        } catch (Throwable t) {
            Notifications.warn("Config", "Clipboard unavailable — use .jay export in log");
            System.out.println("[Jay] CONFIG_EXPORT=" + b64);
        }
    }
}
