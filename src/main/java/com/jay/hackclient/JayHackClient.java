package com.jay.hackclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import com.jay.hackclient.config.ConfigManager;
import com.jay.hackclient.friend.FriendManager;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;
import com.jay.hackclient.render.HudRenderer;

public class JayHackClient implements ClientModInitializer {

    public static final String NAME = "Jay's Hack Client";
    public static final String VERSION = "1.1.1";

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;
    public static FriendManager friendManager;
    public static ConfigManager configManager;

    private static KeyBinding menuKey;
    private static KeyBinding killAuraKey;
    private static KeyBinding sprintKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        moduleManager = new ModuleManager();
        friendManager = new FriendManager();
        configManager = new ConfigManager();

        moduleManager.register(new KillAura());
        moduleManager.register(new TriggerBot());
        moduleManager.register(new AutoClicker());
        moduleManager.register(new AutoSword());
        moduleManager.register(new Criticals());
        moduleManager.register(new Velocity());
        moduleManager.register(new WTap());
        moduleManager.register(new Reach());
        moduleManager.register(new AutoSprint());
        moduleManager.register(new NoSlow());
        moduleManager.register(new Speed());
        moduleManager.register(new ESP());
        moduleManager.register(new FullBright());
        moduleManager.register(new StorageESP());
        moduleManager.register(new HUD());

        // Default HUD on
        Module hud = moduleManager.getModuleByName("HUD");
        if (hud != null) hud.setEnabled(true);

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.clickgui", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, "category.jayhackclient"));
        killAuraKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.toggle_killaura", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R, "category.jayhackclient"));
        sprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.toggle_sprint", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_G, "category.jayhackclient"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (menuKey.wasPressed()) printMenu();
            if (killAuraKey.wasPressed()) toggle("KillAura");
            if (sprintKey.wasPressed()) toggle("AutoSprint");

            moduleManager.onTick();
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            Module h = moduleManager.getModuleByName("HUD");
            if (h != null && h.isEnabled()) {
                HudRenderer.render(context);
            }
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith(".jay")) {
                handleCommand(message);
                return false;
            }
            return true;
        });

        configManager.load();

        System.out.println("[" + NAME + "] v" + VERSION + " loaded — modules: " + moduleManager.getModules().size());
    }

    private void toggle(String name) {
        Module m = moduleManager.getModuleByName(name);
        if (m != null) m.toggle();
    }

    private void printMenu() {
        var p = net.minecraft.client.MinecraftClient.getInstance().player;
        if (p == null) return;
        p.sendMessage(Text.literal("§8§m--------------------------------"), false);
        p.sendMessage(Text.literal("§b" + NAME + " §7v" + VERSION), false);
        p.sendMessage(Text.literal("§8§m--------------------------------"), false);
        for (Module m : moduleManager.getModules()) {
            String st = m.isEnabled() ? "§aON" : "§cOFF";
            p.sendMessage(Text.literal("§7[" + st + "§7] §f" + m.getName()), false);
        }
        p.sendMessage(Text.literal("§8§m--------------------------------"), false);
        p.sendMessage(Text.literal("§7.jay toggle <name> §8| §7.jay friend §8| §7.jay config"), false);
    }

    private void handleCommand(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;

        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            msg("§f.jay list | toggle | friend | config");
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "list", "help", "menu" -> printMenu();
            case "toggle" -> {
                if (args.length < 3) { msg("§cUsage: .jay toggle <module>"); return; }
                Module m = moduleManager.getModuleByName(args[2]);
                if (m == null) msg("§cUnknown module: " + args[2]);
                else m.toggle();
            }
            case "friend", "friends" -> handleFriend(args);
            case "config", "cfg" -> handleConfig(args);
            default -> msg("§cUnknown. Try .jay help");
        }
    }

    private void handleFriend(String[] args) {
        if (args.length < 3) {
            msg("§f.jay friend add/del/list <name>");
            return;
        }
        String action = args[2].toLowerCase();
        if (action.equals("list")) {
            msg("§fFriends: §a" + String.join(", ", friendManager.getFriends()));
            return;
        }
        if (args.length < 4) {
            msg("§cNeed a player name");
            return;
        }
        String name = args[3];
        if (action.equals("add")) {
            friendManager.add(name);
            msg("§aAdded friend §f" + name);
            configManager.save();
        } else if (action.equals("del") || action.equals("remove")) {
            friendManager.remove(name);
            msg("§cRemoved friend §f" + name);
            configManager.save();
        }
    }

    private void handleConfig(String[] args) {
        if (args.length < 3) {
            msg("§f.jay config save | load");
            return;
        }
        String action = args[2].toLowerCase();
        if (action.equals("save")) {
            configManager.save();
            msg("§aConfig saved");
        } else if (action.equals("load")) {
            configManager.load();
            msg("§aConfig loaded");
        }
    }

    private void msg(String s) {
        var p = net.minecraft.client.MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal("§7[§bJay§7] " + s), false);
    }
}
