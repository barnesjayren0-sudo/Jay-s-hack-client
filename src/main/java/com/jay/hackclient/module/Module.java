package com.jay.hackclient.module;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.event.events.ModuleToggleEvent;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.util.Notifications;
import com.jay.hackclient.util.ToggleSounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public enum KeyMode { TOGGLE, HOLD }

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keyBind;
    private KeyMode keyMode = KeyMode.TOGGLE;
    private boolean drawn = true;
    private boolean chatFeedback = true;
    private int runtimeErrorStreak = 0;
    private final List<Setting> settings = new ArrayList<>();

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        this.keyBind = -1;
    }

    protected void addSetting(Setting s) {
        if (s != null) settings.add(s);
    }

    public List<Setting> getSettings() {
        return Collections.unmodifiableList(settings);
    }

    public void resetSettings() {
        for (Setting s : settings) {
            try { s.reset(); } catch (Throwable ignored) {}
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        runtimeErrorStreak = 0;
        if (enabled) {
            try { onEnable(); } catch (Throwable t) {
                System.err.println("[Jay] " + name + " onEnable: " + t.getMessage());
                this.enabled = false;
                return;
            }
            notify("enabled");
            try { ToggleSounds.play(true); } catch (Throwable ignored) {}
            try { Notifications.push(name, "enabled"); } catch (Throwable ignored) {}
        } else {
            try { onDisable(); } catch (Throwable t) {
                System.err.println("[Jay] " + name + " onDisable: " + t.getMessage());
            }
            notify("disabled");
            try { ToggleSounds.play(false); } catch (Throwable ignored) {}
            try { Notifications.push(name, "disabled"); } catch (Throwable ignored) {}
        }
        try {
            if (JayHackClient.EVENT_BUS != null) {
                JayHackClient.EVENT_BUS.post(new ModuleToggleEvent(this, this.enabled));
            }
        } catch (Throwable ignored) {}
    }

    private void notify(String status) {
        if (!chatFeedback) return;
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] §f" + name + " §7" + status), false);
        }
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}

    public final void markTickHealthy() {
        runtimeErrorStreak = 0;
    }

    public final int markTickError() {
        return ++runtimeErrorStreak;
    }

    public final int getRuntimeErrorStreak() {
        return runtimeErrorStreak;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public int getKeyBind() { return keyBind; }
    public void setKeyBind(int keyBind) { this.keyBind = keyBind; }
    public KeyMode getKeyMode() { return keyMode; }
    public void setKeyMode(KeyMode mode) { if (mode != null) this.keyMode = mode; }
    public boolean isDrawn() { return drawn; }
    public void setDrawn(boolean drawn) { this.drawn = drawn; }
    public boolean isChatFeedback() { return chatFeedback; }
    public void setChatFeedback(boolean chatFeedback) { this.chatFeedback = chatFeedback; }

    public int getCategoryColor() {
        return switch (category) {
            case COMBAT -> 0xFFFF5555;
            case MOVEMENT -> 0xFF55FF55;
            case RENDER -> 0xFF55FFFF;
            case PLAYER -> 0xFFFFFF55;
            case WORLD -> 0xFFAA55FF;
            case ANARCHY -> 0xFFFFAA00;
            case MISC -> 0xFFAAAAAA;
        };
    }

    public String getKeyLabel() {
        if (keyBind < 0) return "";
        String n = GLFW.glfwGetKeyName(keyBind, 0);
        if (n != null) return n.toUpperCase();
        return switch (keyBind) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            default -> "#" + keyBind;
        };
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        PLAYER("Player"),
        WORLD("World"),
        ANARCHY("Anarchy"),
        MISC("Misc");

        public final String displayName;
        Category(String displayName) { this.displayName = displayName; }
    }
}
