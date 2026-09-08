package com.jay.client18.module;

import com.jay.client18.JayClient18;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;

public abstract class Module {

    protected static final Minecraft mc = Minecraft.getMinecraft();

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private int keyBind;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        this.keyBind = Keyboard.KEY_NONE;
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) {
            try { onEnable(); } catch (Throwable t) {
                this.enabled = false;
                System.err.println("[Jay18] " + name + " onEnable: " + t.getMessage());
                return;
            }
            JayClient18.msg("§f" + name + " §7enabled");
        } else {
            try { onDisable(); } catch (Throwable t) {
                System.err.println("[Jay18] " + name + " onDisable: " + t.getMessage());
            }
            JayClient18.msg("§f" + name + " §7disabled");
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
    public void setKeyBind(int key) { this.keyBind = key; }

    public String getKeyLabel() {
        if (keyBind == Keyboard.KEY_NONE) return "";
        return Keyboard.getKeyName(keyBind);
    }

    public enum Category {
        COMBAT("Combat"),
        MOVEMENT("Movement"),
        RENDER("Render"),
        PLAYER("Player"),
        MISC("Misc");

        public final String display;
        Category(String display) { this.display = display; }
    }
}
