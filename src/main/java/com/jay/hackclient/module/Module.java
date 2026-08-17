package com.jay.hackclient.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public abstract class Module {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

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
        this.keyBind = -1;
    }

    public void toggle() {
        setEnabled(!this.enabled);
    }

    public void setEnabled(boolean state) {
        if (this.enabled == state) return;
        this.enabled = state;
        if (state) {
            onEnable();
            notify("§aenabled");
        } else {
            onDisable();
            notify("§cdisabled");
        }
    }

    private void notify(String status) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] §f" + name + " " + status), false);
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
