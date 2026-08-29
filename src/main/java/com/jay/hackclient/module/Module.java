package com.jay.hackclient.module;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.setting.Setting;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keyBind;
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
        } else {
            onDisable();
            notify("disabled");
            Notifications.push(name, "disabled");
        }
        if (JayHackClient.configManager != null) {
            try {
                JayHackClient.configManager.save();
            } catch (Exception ignored) {}
        }
    }

    private void notify(String status) {
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

    public String getKeyLabel() {
        if (keyBind < 0) return "";
        return switch (keyBind) {
            case GLFW.GLFW_KEY_R -> "R";
            case GLFW.GLFW_KEY_N -> "N";
            case GLFW.GLFW_KEY_P -> "P";
            case GLFW.GLFW_KEY_G -> "G";
            case GLFW.GLFW_KEY_H -> "H";
            case GLFW.GLFW_KEY_J -> "J";
            case GLFW.GLFW_KEY_K -> "K";
            case GLFW.GLFW_KEY_L -> "L";
            case GLFW.GLFW_KEY_V -> "V";
            case GLFW.GLFW_KEY_B -> "B";
            case GLFW.GLFW_KEY_X -> "X";
            case GLFW.GLFW_KEY_C -> "C";
            case GLFW.GLFW_KEY_Z -> "Z";
            case GLFW.GLFW_KEY_F -> "F";
            case GLFW.GLFW_KEY_T -> "T";
            case GLFW.GLFW_KEY_Y -> "Y";
            case GLFW.GLFW_KEY_U -> "U";
            case GLFW.GLFW_KEY_I -> "I";
            case GLFW.GLFW_KEY_O -> "O";
            case GLFW.GLFW_KEY_M -> "M";
            case GLFW.GLFW_KEY_DELETE -> "DEL";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
            default -> {
                String n = GLFW.glfwGetKeyName(keyBind, 0);
                yield n != null ? n.toUpperCase() : "#" + keyBind;
            }
        };
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc");

        public final String displayName;
        Category(String displayName) { this.displayName = displayName; }
    }
}
