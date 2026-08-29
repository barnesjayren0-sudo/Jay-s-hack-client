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

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;
        this.enabled = state;
        if (state) {
            onEnable();
            notify("enabled");
            Notifications.push(name, "enabled");
            ToggleSounds.play(true);
        } else {
            onDisable();
            notify("disabled");
            Notifications.push(name, "disabled");
            ToggleSounds.play(false);
        }
        if (JayHackClient.EVENT_BUS != null) {
            try {
                JayHackClient.EVENT_BUS.post(new ModuleToggleEvent(this, state));
            } catch (Throwable ignored) {}
        }
        if (JayHackClient.configManager != null) {
            try {
                JayHackClient.configManager.save();
            } catch (Exception ignored) {}
        }
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
