package com.jay.hackclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;

public class JayHackClient implements ClientModInitializer {

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;

    private static KeyBinding clickGuiKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        moduleManager = new ModuleManager();

        // Register Sword PvP modules
        moduleManager.register(new KillAura());
        moduleManager.register(new AutoSword());
        moduleManager.register(new Reach());
        moduleManager.register(new Criticals());
        moduleManager.register(new AutoSprint());
        moduleManager.register(new Velocity());
        moduleManager.register(new ESP());
        moduleManager.register(new NoSlow());

        // ClickGUI key (Right Shift)
        clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.jayhackclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (clickGuiKey.wasPressed()) {
                // Open ClickGUI here later
                client.player.sendMessage(net.minecraft.text.Text.literal("§bJay's Hack Client §f- ClickGUI (coming soon)"), false);
            }

            moduleManager.onTick();
        });

        System.out.println("[Jay's Hack Client] Loaded - Sword PvP Focused (1.21.11)");
    }
}
