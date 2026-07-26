package com.jay.hackclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;

public class JayHackClient implements ClientModInitializer {

    public static final String NAME = "Jay's Hack Client";
    public static final String VERSION = "1.0.0";

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;

    private static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        moduleManager = new ModuleManager();

        // Register all Sword PvP modules
        moduleManager.register(new KillAura());
        moduleManager.register(new AutoSword());
        moduleManager.register(new Reach());
        moduleManager.register(new Criticals());
        moduleManager.register(new AutoSprint());
        moduleManager.register(new Velocity());
        moduleManager.register(new ESP());
        moduleManager.register(new NoSlow());

        // Right Shift = ClickGUI / Info
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.jayhackclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (clickGuiKey.wasPressed() && client.player != null) {
                client.player.sendMessage(Text.literal("§7[§bJay§7] §f" + NAME + " v" + VERSION + " - Sword PvP"), false);
                client.player.sendMessage(Text.literal("§7Modules loaded: §a" + moduleManager.getModules().size()), false);
            }

            moduleManager.onTick();
        });

        System.out.println("========================================");
        System.out.println("[" + NAME + "] v" + VERSION + " loaded");
        System.out.println("[" + NAME + "] Sword PvP Focused Client");
        System.out.println("[" + NAME + "] Modules: " + moduleManager.getModules().size());
        System.out.println("========================================");
    }
}
