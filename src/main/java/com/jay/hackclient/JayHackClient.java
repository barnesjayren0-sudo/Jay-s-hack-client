package com.jay.hackclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import com.jay.hackclient.config.ConfigManager;
import com.jay.hackclient.friend.FriendManager;
import com.jay.hackclient.gui.ClickGuiScreen;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;
import com.jay.hackclient.profile.LegitProfile;
import com.jay.hackclient.render.HudRenderer;

public class JayHackClient implements ClientModInitializer {

    public static final String NAME = "Jay's Hack Client";
    public static final String VERSION = "1.5.0";

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;
    public static FriendManager friendManager;
    public static ConfigManager configManager;

    private static KeyBinding menuKey;
    private static KeyBinding killAuraKey;
    private static KeyBinding sprintKey;
    private static KeyBinding panicKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        moduleManager = new ModuleManager();
        friendManager = new FriendManager();
        configManager = new ConfigManager();

        // Combat
        moduleManager.register(new KillAura());
        moduleManager.register(new AimAssist());
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
        moduleManager.register(new NoFall());

        // Render
        moduleManager.register(new ESP());
        moduleManager.register(new Nametags());
        moduleManager.register(new FullBright());
        moduleManager.register(new StorageESP());
        moduleManager.register(new TargetHUD());
        moduleManager.register(new HUD());

        // Player
        moduleManager.register(new AutoArmor());

        // World
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
        panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.panic", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DELETE, "category.jayhackclient"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (panicKey.wasPressed()) {
                moduleManager.panic();
                client.player.sendMessage(Text.literal("§8[§cPANIC§8] §fAll off · §7.jay unpanic"), false);
            }

            if (menuKey.wasPressed()) {
                if (client.currentScreen instanceof ClickGuiScreen) client.setScreen(null);
                else if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }

            if (killAuraKey.wasPressed()) toggle("KillAura");
            if (sprintKey.wasPressed()) toggle("AutoSprint");

            moduleManager.onTick();
        });

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            if (moduleManager.isFrozen()) return;
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
        System.out.println("[" + NAME + "] v" + VERSION + " — " + moduleManager.getModules().size() + " modules");
    }

    private void toggle(String name) {
        if (moduleManager.isFrozen()) {
            msg("§cUnpanic first");
            return;
        }
        Module m = moduleManager.getModuleByName(name);
        if (m != null) m.toggle();
    }

    private void handleCommand(String message) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            msg("§fgui toggle off panic unpanic profile scan radar friend config");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "gui", "clickgui", "menu", "list", "help" -> client.setScreen(new ClickGuiScreen());
            case "toggle" -> {
                if (args.length < 3) { msg("§c.jay toggle <module>"); return; }
                toggle(args[2]);
            }
            case "off", "disableall", "disable" -> {
                moduleManager.disableAll();
                msg("§eAll off");
            }
            case "panic" -> {
                moduleManager.panic();
                msg("§cPANIC");
            }
            case "unpanic", "unfreeze" -> {
                moduleManager.unfreeze();
                msg("§aUnfrozen");
            }
            case "profile" -> {
                if (args.length < 3) { msg("§flegit|semi|rage|scout"); return; }
                switch (args[2].toLowerCase()) {
                    case "legit" -> { LegitProfile.applyLegit(); msg("§aLegit"); }
                    case "semi" -> { LegitProfile.applySemi(); msg("§eSemi"); }
                    case "rage" -> { LegitProfile.applyRage(); msg("§cRage"); }
                    case "scout" -> { LegitProfile.applyScout(); msg("§bScout"); }
                    default -> msg("§cUnknown profile");
                }
            }
            case "scan" -> {
                Module bf = moduleManager.getModuleByName("BaseFinder");
                if (bf instanceof BaseFinder f) f.scan(true);
            }
            case "radar" -> {
                Module pr = moduleManager.getModuleByName("PlayerRadar");
                if (pr instanceof PlayerRadar r) r.report(true);
            }
            case "friend", "friends" -> handleFriend(args);
            case "config", "cfg" -> handleConfig(args);
            default -> msg("§cUnknown");
        }
    }

    private void handleFriend(String[] args) {
        if (args.length < 3) { msg("§ffriend add|del|list"); return; }
        String a = args[2].toLowerCase();
        if (a.equals("list")) {
            msg("§f" + String.join(", ", friendManager.getFriends()));
            return;
        }
        if (args.length < 4) return;
        if (a.equals("add")) {
            friendManager.add(args[3]);
            configManager.save();
            msg("§a+ " + args[3]);
        } else if (a.equals("del") || a.equals("remove")) {
            friendManager.remove(args[3]);
            configManager.save();
            msg("§c- " + args[3]);
        }
    }

    private void handleConfig(String[] args) {
        if (args.length < 3) return;
        if (args[2].equalsIgnoreCase("save")) {
            configManager.save();
            msg("§aSaved");
        } else if (args[2].equalsIgnoreCase("load")) {
            configManager.load();
            msg("§aLoaded");
        }
    }

    private void msg(String s) {
        var p = MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }
}
