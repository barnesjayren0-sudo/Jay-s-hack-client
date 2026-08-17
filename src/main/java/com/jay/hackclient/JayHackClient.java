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
    public static final String VERSION = "1.2.0";

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
        moduleManager.register(new Speed());

        // Render
        moduleManager.register(new ESP());
        moduleManager.register(new FullBright());
        moduleManager.register(new StorageESP());
        moduleManager.register(new HUD());

        // World / Base finders
        moduleManager.register(new BaseFinder());
        moduleManager.register(new SpawnerFinder());
        moduleManager.register(new PlayerRadar());
        moduleManager.register(new PortalFinder());

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
            if (h != null && h.isEnabled()) HudRenderer.render(context);
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith(".jay")) {
                handleCommand(message);
                return false;
            }
            return true;
        });

        configManager.load();
        System.out.println("[" + NAME + "] v" + VERSION + " ready — " + moduleManager.getModules().size() + " modules");
    }

    private void toggle(String name) {
        Module m = moduleManager.getModuleByName(name);
        if (m != null) m.toggle();
    }

    private void printMenu() {
        var p = net.minecraft.client.MinecraftClient.getInstance().player;
        if (p == null) return;
        p.sendMessage(Text.literal("§8§m──────── §bJay §fv" + VERSION + " §8§m────────"), false);
        Module.Category last = null;
        for (Module m : moduleManager.getModules()) {
            if (m.getCategory() != last) {
                last = m.getCategory();
                p.sendMessage(Text.literal("§8▪ §d" + last.displayName), false);
            }
            String st = m.isEnabled() ? "§a●" : "§7○";
            p.sendMessage(Text.literal("  " + st + " §f" + m.getName() + " §8— §7" + m.getDescription()), false);
        }
        p.sendMessage(Text.literal("§8§m────────────────────────────"), false);
        p.sendMessage(Text.literal("§7.jay toggle §8| §7scan §8| §7radar §8| §7friend §8| §7config"), false);
    }

    private void handleCommand(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            msg("§f.jay list | toggle | scan | radar | friend | config");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list", "help", "menu" -> printMenu();
            case "toggle" -> {
                if (args.length < 3) { msg("§c.jay toggle <module>"); return; }
                Module m = moduleManager.getModuleByName(args[2]);
                if (m == null) msg("§cUnknown: " + args[2]);
                else m.toggle();
            }
            case "scan" -> {
                Module bf = moduleManager.getModuleByName("BaseFinder");
                if (bf instanceof BaseFinder finder) finder.scan(true);
                else msg("§cBaseFinder missing");
            }
            case "radar" -> {
                Module pr = moduleManager.getModuleByName("PlayerRadar");
                if (pr instanceof PlayerRadar radar) radar.report(true);
                else msg("§cPlayerRadar missing");
            }
            case "friend", "friends" -> handleFriend(args);
            case "config", "cfg" -> handleConfig(args);
            default -> msg("§cUnknown command");
        }
    }

    private void handleFriend(String[] args) {
        if (args.length < 3) { msg("§f.jay friend add|del|list"); return; }
        String action = args[2].toLowerCase();
        if (action.equals("list")) {
            msg("§fFriends: §a" + String.join(", ", friendManager.getFriends()));
            return;
        }
        if (args.length < 4) { msg("§cNeed name"); return; }
        String name = args[3];
        if (action.equals("add")) {
            friendManager.add(name);
            msg("§a+ friend §f" + name);
            configManager.save();
        } else if (action.equals("del") || action.equals("remove")) {
            friendManager.remove(name);
            msg("§c- friend §f" + name);
            configManager.save();
        }
    }

    private void handleConfig(String[] args) {
        if (args.length < 3) { msg("§f.jay config save|load"); return; }
        if (args[2].equalsIgnoreCase("save")) {
            configManager.save();
            msg("§aConfig saved");
        } else if (args[2].equalsIgnoreCase("load")) {
            configManager.load();
            msg("§aConfig loaded");
        }
    }

    private void msg(String s) {
        var p = net.minecraft.client.MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }
}
