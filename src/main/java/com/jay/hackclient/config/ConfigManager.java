package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
            sb.append("# Jay Hack Client config\n");
            sb.append("aimMode=").append(ClientSettings.aimMode).append('\n');
            sb.append("targetPriority=").append(ClientSettings.targetPriority).append('\n');
            sb.append("aimRange=").append(ClientSettings.aimRange).append('\n');
            sb.append("aimFov=").append(ClientSettings.aimFov).append('\n');
            sb.append("aimSmooth=").append(ClientSettings.aimSmooth).append('\n');
            sb.append("auraRange=").append(ClientSettings.auraRange).append('\n');
            sb.append("hitboxExpand=").append(ClientSettings.hitboxExpand).append('\n');
            sb.append("velocityMode=").append(ClientSettings.velocityMode).append('\n');
            sb.append("velocityHorizontal=").append(ClientSettings.velocityHorizontal).append('\n');
            sb.append("velocityVertical=").append(ClientSettings.velocityVertical).append('\n');
            sb.append("combatDelayMin=").append(ClientSettings.combatDelayMin).append('\n');
            sb.append("combatDelayMax=").append(ClientSettings.combatDelayMax).append('\n');
            sb.append("missChance=").append(ClientSettings.missChance).append('\n');
            sb.append("pingScaleDelays=").append(ClientSettings.pingScaleDelays).append('\n');
            sb.append("hideHudOnScreenshot=").append(ClientSettings.hideHudOnScreenshot).append('\n');
            sb.append("hideHudInDebug=").append(ClientSettings.hideHudInDebug).append('\n');
            sb.append("mode=").append(ClientSettings.mode).append('\n');

            if (JayHackClient.moduleManager != null) {
                for (Module m : JayHackClient.moduleManager.getModules()) {
                    sb.append("mod.").append(m.getName()).append('=').append(m.isEnabled()).append('\n');
                }
            }
            if (JayHackClient.friendManager != null) {
                for (String f : JayHackClient.friendManager.getFriends()) {
                    sb.append("friend.").append(f).append("=true\n");
                }
            }
            Files.writeString(p, sb.toString());
        } catch (IOException e) {
            System.err.println("[Jay] config save failed: " + e.getMessage());
        }
    }

    public void load() {
        try {
            Path p = path();
            if (!Files.exists(p)) return;
            for (String line : Files.readAllLines(p)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) continue;
                String[] kv = line.split("=", 2);
                String k = kv[0].trim();
                String v = kv[1].trim();
                switch (k) {
                    case "aimMode" -> ClientSettings.aimMode = v;
                    case "targetPriority" -> ClientSettings.targetPriority = v;
                    case "aimRange" -> ClientSettings.aimRange = dbl(v, ClientSettings.aimRange);
                    case "aimFov" -> ClientSettings.aimFov = (float) dbl(v, ClientSettings.aimFov);
                    case "aimSmooth" -> ClientSettings.aimSmooth = (float) dbl(v, ClientSettings.aimSmooth);
                    case "auraRange" -> ClientSettings.auraRange = dbl(v, ClientSettings.auraRange);
                    case "hitboxExpand" -> {
                        ClientSettings.hitboxExpand = dbl(v, ClientSettings.hitboxExpand);
                        Hitboxes.setExpand(ClientSettings.hitboxExpand);
                    }
                    case "velocityMode" -> ClientSettings.applyVelocityMode(v);
                    case "velocityHorizontal" -> ClientSettings.velocityHorizontal = dbl(v, ClientSettings.velocityHorizontal);
                    case "velocityVertical" -> ClientSettings.velocityVertical = dbl(v, ClientSettings.velocityVertical);
                    // legacy key from older configs
                    case "velocityFactor" -> {
                        ClientSettings.velocityHorizontal = dbl(v, ClientSettings.velocityHorizontal);
                        ClientSettings.velocityVertical = Math.max(ClientSettings.velocityVertical, 0.85);
                    }
                    case "combatDelayMin" -> ClientSettings.combatDelayMin = (int) dbl(v, ClientSettings.combatDelayMin);
                    case "combatDelayMax" -> ClientSettings.combatDelayMax = (int) dbl(v, ClientSettings.combatDelayMax);
                    case "missChance" -> ClientSettings.missChance = (int) dbl(v, ClientSettings.missChance);
                    case "pingScaleDelays" -> ClientSettings.pingScaleDelays = Boolean.parseBoolean(v);
                    case "hideHudOnScreenshot" -> ClientSettings.hideHudOnScreenshot = Boolean.parseBoolean(v);
                    case "hideHudInDebug" -> ClientSettings.hideHudInDebug = Boolean.parseBoolean(v);
                    case "mode" -> ClientSettings.mode = v;
                    default -> {
                        if (k.startsWith("mod.") && JayHackClient.moduleManager != null) {
                            Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                            if (m != null) m.setEnabled(Boolean.parseBoolean(v));
                        } else if (k.startsWith("friend.") && JayHackClient.friendManager != null) {
                            JayHackClient.friendManager.add(k.substring(7));
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[Jay] config load failed: " + e.getMessage());
        }
    }

    private double dbl(String s, double def) {
        try { return Double.parseDouble(s); } catch (Exception e) { return def; }
    }
}
