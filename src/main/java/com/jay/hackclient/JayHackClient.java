package com.jay.hackclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;

public class JayHackClient implements ClientModInitializer {

    public static final String NAME = "Jay's Hack Client";
    public static final String VERSION = "1.1.0";

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;

    private static KeyBinding menuKey;
    private static KeyBinding killAuraKey;
    private static KeyBinding sprintKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        moduleManager = new ModuleManager();

        // Combat
        moduleManager.register(new KillAura());
        moduleManager.register(new TriggerBot());
        moduleManager.register(new AutoClicker());
        moduleManager.register(new AutoSword());
        moduleManager.register(new Criticals());
        moduleManager.register(new Velocity());
        moduleManager.register(new WTap());
        moduleManager.register(new Reach());

        // Movement
        moduleManager.register(new AutoSprint());
        moduleManager.register(new NoSlow());

        // Render
        moduleManager.register(new ESP());
        moduleManager.register(new FullBright());

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.clickgui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.jayhackclient"
        ));

        killAuraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.toggle_killaura",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.jayhackclient"
        ));

        sprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.toggle_sprint",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                "category.jayhackclient"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (menuKey.wasPressed()) {
                printMenu(client.player);
            }
            if (killAuraKey.wasPressed()) {
                Module m = moduleManager.getModuleByName("KillAura");
                if (m != null) m.toggle();
            }
            if (sprintKey.wasPressed()) {
                Module m = moduleManager.getModuleByName("AutoSprint");
                if (m != null) m.toggle();
            }

            moduleManager.onTick();
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith(".jay")) {
                handleCommand(message);
                return false;
            }
            return true;
        });

        System.out.println("========================================");
        System.out.println("[" + NAME + "] v" + VERSION + " loaded");
        System.out.println("[" + NAME + "] Modules: " + moduleManager.getModules().size());
        System.out.println("========================================");
    }

    private void printMenu(net.minecraft.entity.player.PlayerEntity player) {
        player.sendMessage(Text.literal("§8§m---------------------------"), false);
        player.sendMessage(Text.literal("§b" + NAME + " §7v" + VERSION), false);
        player.sendMessage(Text.literal("§7Sword PvP · Fabric 1.21.11"), false);
        player.sendMessage(Text.literal("§8§m---------------------------"), false);
        for (Module m : moduleManager.getModules()) {
            String state = m.isEnabled() ? "§aON" : "§cOFF";
            player.sendMessage(Text.literal("§7[" + state + "§7] §f" + m.getName() + " §8- §7" + m.getDescription()), false);
        }
        player.sendMessage(Text.literal("§8§m---------------------------"), false);
        player.sendMessage(Text.literal("§7Chat: §f.jay toggle <name> §7| §f.jay list"), false);
    }

    private void handleCommand(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;

        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            client.player.sendMessage(Text.literal("§7[§bJay§7] §f.jay list | .jay toggle <module>"), false);
            return;
        }

        String sub = args[1].toLowerCase();
        if (sub.equals("list") || sub.equals("help")) {
            printMenu(client.player);
            return;
        }
        if (sub.equals("toggle") && args.length >= 3) {
            Module m = moduleManager.getModuleByName(args[2]);
            if (m == null) {
                client.player.sendMessage(Text.literal("§7[§bJay§7] §cModule not found: " + args[2]), false);
            } else {
                m.toggle();
            }
            return;
        }
        client.player.sendMessage(Text.literal("§7[§bJay§7] §cUnknown command"), false);
    }
}
