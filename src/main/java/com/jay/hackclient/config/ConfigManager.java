package com.jay.hackclient.config;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.gui.GuiLayout;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import com.jay.hackclient.module.modules.Reach;
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
            sb.append("# Jay Hack Client config v1.36\n");
            sb.append("aimMode=").append(ClientSettings.aimMode).append('\n');
            sb.append("targetPriority=").append(ClientSettings.targetPriority).append('\n');
            sb.append("aimRange=").append(ClientSettings.aimRange).append('\n');
            sb.append("aimFov=").append(ClientSettings.aimFov).append('\n');
            sb.append("aimSmooth=").append(ClientSettings.aimSmooth).append('\n');
            sb.append("aimDeadzone=").append(ClientSettings.aimDeadzone).append('\n');
            sb.append("aimMaxStep=").append(ClientSettings.aimMaxStep).append('\n');
            sb.append("auraRange=").append(ClientSettings.auraRange).append('\n');
            sb.append("auraFov=").append(ClientSettings.auraFov).append('\n');
            sb.append("auraMultiTarget=").append(ClientSettings.auraMultiTarget).append('\n');
            sb.append("hitboxExpand=").append(ClientSettings.hitboxExpand).append('\n');
            sb.append("reachDistance=").append(ClientSettings.reachDistance).append('\n');
            sb.append("reachMode=").append(ClientSettings.reachMode).append('\n');
            sb.append("velocityMode=").append(ClientSettings.velocityMode).append('\n');
            sb.append("velocityHorizontal=").append(ClientSettings.velocityHorizontal).append('\n');
            sb.append("velocityVertical=").append(ClientSettings.velocityVertical).append('\n');
            sb.append("velocityOnlyWhenHurt=").append(ClientSettings.velocityOnlyWhenHurt).append('\n');
            sb.append("combatDelayMin=").append(ClientSettings.combatDelayMin).append('\n');
            sb.append("combatDelayMax=").append(ClientSettings.combatDelayMax).append('\n');
            sb.append("clickDelayMin=").append(ClientSettings.clickDelayMin).append('\n');
            sb.append("clickDelayMax=").append(ClientSettings.clickDelayMax).append('\n');
            sb.append("missChance=").append(ClientSettings.missChance).append('\n');
            sb.append("tickSkipChance=").append(ClientSettings.tickSkipChance).append('\n');
            sb.append("requireAttackKey=").append(ClientSettings.requireAttackKey).append('\n');
            sb.append("cooldownCheck=").append(ClientSettings.cooldownCheck).append('\n');
            sb.append("critTiming=").append(ClientSettings.critTiming).append('\n');
            sb.append("pingScaleDelays=").append(ClientSettings.pingScaleDelays).append('\n');
            sb.append("hideHudOnScreenshot=").append(ClientSettings.hideHudOnScreenshot).append('\n');
            sb.append("hideHudInDebug=").append(ClientSettings.hideHudInDebug).append('\n');
            sb.append("mode=").append(ClientSettings.mode).append('\n');
            sb.append("lastProfile=").append(ClientSettings.lastProfile).append('\n');
            sb.append("potSlotMin=").append(ClientSettings.potSlotMin).append('\n');
            sb.append("potSlotMax=").append(ClientSettings.potSlotMax).append('\n');
            sb.append("arrayListColor=").append(ClientSettings.arrayListColor).append('\n');
            sb.append("arrayListRainbow=").append(ClientSettings.arrayListRainbow).append('\n');
            sb.append("toggleSounds=").append(ClientSettings.toggleSounds).append('\n');
            sb.append("showActiveCount=").append(ClientSettings.showActiveCount).append('\n');

            if (JayHackClient.moduleManager != null) {
                for (Module m : JayHackClient.moduleManager.getModules()) {
                    sb.append("mod.").append(m.getName()).append('=').append(m.isEnabled()).append('\n');
                    sb.append("key.").append(m.getName()).append('=').append(m.getKeyBind()).append('\n');
                    sb.append("drawn.").append(m.getName()).append('=').append(m.isDrawn()).append('\n');
                    sb.append("keymode.").append(m.getName()).append('=').append(m.getKeyMode().name()).append('\n');
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
            GuiLayout.ensureDefaults();
            for (var e : GuiLayout.POS.entrySet()) {
                float[] xy = e.getValue();
                sb.append("panel.").append(e.getKey().name()).append('=')
                        .append(xy[0]).append(',').append(xy[1]).append('\n');
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
            ClientSettings.favorites.clear();
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
                    case "aimDeadzone" -> ClientSettings.aimDeadzone = (float) dbl(v, ClientSettings.aimDeadzone);
                    case "aimMaxStep" -> ClientSettings.aimMaxStep = (float) dbl(v, ClientSettings.aimMaxStep);
                    case "auraRange" -> ClientSettings.auraRange = dbl(v, ClientSettings.auraRange);
                    case "auraFov" -> ClientSettings.auraFov = (float) dbl(v, ClientSettings.auraFov);
                    case "auraMultiTarget" -> ClientSettings.auraMultiTarget = Boolean.parseBoolean(v);
                    case "hitboxExpand" -> {
                        ClientSettings.hitboxExpand = dbl(v, ClientSettings.hitboxExpand);
                        Hitboxes.setExpand(ClientSettings.hitboxExpand);
                    }
                    case "reachDistance" -> ClientSettings.reachDistance = Reach.clamp(dbl(v, ClientSettings.reachDistance));
                    case "reachMode" -> ClientSettings.reachMode = v;
                    case "velocityMode" -> ClientSettings.applyVelocityMode(v);
                    case "velocityHorizontal" -> ClientSettings.velocityHorizontal = dbl(v, ClientSettings.velocityHorizontal);
                    case "velocityVertical" -> ClientSettings.velocityVertical = dbl(v, ClientSettings.velocityVertical);
                    case "velocityOnlyWhenHurt" -> ClientSettings.velocityOnlyWhenHurt = Boolean.parseBoolean(v);
                    case "combatDelayMin" -> ClientSettings.combatDelayMin = (int) dbl(v, ClientSettings.combatDelayMin);
                    case "combatDelayMax" -> ClientSettings.combatDelayMax = (int) dbl(v, ClientSettings.combatDelayMax);
                    case "clickDelayMin" -> ClientSettings.clickDelayMin = (int) dbl(v, ClientSettings.clickDelayMin);
                    case "clickDelayMax" -> ClientSettings.clickDelayMax = (int) dbl(v, ClientSettings.clickDelayMax);
                    case "missChance" -> ClientSettings.missChance = (int) dbl(v, ClientSettings.missChance);
                    case "tickSkipChance" -> ClientSettings.tickSkipChance = (int) dbl(v, ClientSettings.tickSkipChance);
                    case "requireAttackKey" -> ClientSettings.requireAttackKey = Boolean.parseBoolean(v);
                    case "cooldownCheck" -> ClientSettings.cooldownCheck = Boolean.parseBoolean(v);
                    case "critTiming" -> ClientSettings.critTiming = Boolean.parseBoolean(v);
                    case "pingScaleDelays" -> ClientSettings.pingScaleDelays = Boolean.parseBoolean(v);
                    case "hideHudOnScreenshot" -> ClientSettings.hideHudOnScreenshot = Boolean.parseBoolean(v);
                    case "hideHudInDebug" -> ClientSettings.hideHudInDebug = Boolean.parseBoolean(v);
                    case "mode" -> ClientSettings.mode = v;
                    case "lastProfile" -> ClientSettings.lastProfile = v;
                    case "potSlotMin" -> ClientSettings.potSlotMin = (int) dbl(v, ClientSettings.potSlotMin);
                    case "potSlotMax" -> ClientSettings.potSlotMax = (int) dbl(v, ClientSettings.potSlotMax);
                    case "arrayListColor" -> ClientSettings.arrayListColor = (int) dbl(v, ClientSettings.arrayListColor);
                    case "arrayListRainbow" -> ClientSettings.arrayListRainbow = Boolean.parseBoolean(v);
                    case "toggleSounds" -> ClientSettings.toggleSounds = Boolean.parseBoolean(v);
                    case "showActiveCount" -> ClientSettings.showActiveCount = Boolean.parseBoolean(v);
                    default -> {
                        if (k.startsWith("mod.") && JayHackClient.moduleManager != null) {
                            Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                            if (m != null) m.setEnabled(Boolean.parseBoolean(v));
                        } else if (k.startsWith("key.") && JayHackClient.moduleManager != null) {
                            Module m = JayHackClient.moduleManager.getModuleByName(k.substring(4));
                            if (m != null) {
                                try { m.setKeyBind(Integer.parseInt(v)); } catch (Exception ignored) {}
                            }
                        } else if (k.startsWith("drawn.") && JayHackClient.moduleManager != null) {
                            Module m = JayHackClient.moduleManager.getModuleByName(k.substring(6));
                            if (m != null) m.setDrawn(Boolean.parseBoolean(v));
                        } else if (k.startsWith("keymode.") && JayHackClient.moduleManager != null) {
                            Module m = JayHackClient.moduleManager.getModuleByName(k.substring(8));
                            if (m != null) {
                                try { m.setKeyMode(Module.KeyMode.valueOf(v)); } catch (Exception ignored) {}
                            }
                        } else if (k.startsWith("friend.") && JayHackClient.friendManager != null) {
                            JayHackClient.friendManager.add(k.substring(7));
                        } else if (k.startsWith("fav.")) {
                            ClientSettings.addFavorite(k.substring(4));
                        } else if (k.startsWith("panel.")) {
                            try {
                                Module.Category cat = Module.Category.valueOf(k.substring(6));
                                String[] parts = v.split(",");
                                GuiLayout.set(cat, Float.parseFloat(parts[0]), Float.parseFloat(parts[1]));
                            } catch (Exception ignored) {}
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
