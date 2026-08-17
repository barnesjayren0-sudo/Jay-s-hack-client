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
import com.jay.hackclient.profile.LegitProfile;
import com.jay.hackclient.render.HudRenderer;

public class JayHackClient implements ClientModInitializer {

    public static final String NAME = "Jay's Hack Client";
    public static final String VERSION = "1.3.0";

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
        moduleManager.register(new BaseFinder());
        moduleManager.register(new SpawnerFinder());
        moduleManager.register(new PlayerRadar());
        moduleManager.register(new PortalFinder());

        // Safe default: only HUD
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
                client.player.sendMessage(Text.literal("§8[§cPANIC§8] §fAll modules disabled. §7.jay unpanic to restore"), false);
            }
            if (menuKey.wasPressed()) printMenu();
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
            msg("§cClient frozen — .jay unpanic first");
            return;
        }
        Module m = moduleManager.getModuleByName(name);
        if (m != null) m.toggle();
    }

    private void printMenu() {
        var p = net.minecraft.client.MinecraftClient.getInstance().player;
        if (p == null) return;
        String freeze = moduleManager.isFrozen() ? " §c[FROZEN]" : "";
        p.sendMessage(Text.literal("§8§m────── §bJay §fv" + VERSION + freeze + " §8§m──────"), false);
        Module.Category last = null;
        for (Module m : moduleManager.getModules()) {
            if (m.getCategory() != last) {
                last = m.getCategory();
                p.sendMessage(Text.literal("§8▪ §d" + last.displayName), false);
            }
            String st = m.isEnabled() ? "§a●" : "§7○";
            p.sendMessage(Text.literal("  " + st + " §f" + m.getName()), false);
        }
        p.sendMessage(Text.literal("§8§m────────────────────────────"), false);
        p.sendMessage(Text.literal("§7DEL=panic §8| §7.jay off §8| §7.jay profile legit/semi/rage/scout"), false);
    }

    private void handleCommand(String message) {
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            msg("§flist toggle off panic unpanic profile scan radar friend config");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "list", "help", "menu" -> printMenu();
            case "toggle" -> {
                if (args.length < 3) { msg("§c.jay toggle <module>"); return; }
                toggle(args[2]);
            }
            case "off", "disableall", "disable" -> {
                moduleManager.disableAll();
                msg("§eAll modules off");
            }
            case "panic" -> {
                moduleManager.panic();
                msg("§cPANIC — everything off & frozen");
            }
            case "unpanic", "unfreeze" -> {
                moduleManager.unfreeze();
                msg("§aUnfrozen — toggle modules again");
            }
            case "profile" -> {
                if (args.length < 3) {
                    msg("§f.jay profile legit | semi | rage | scout");
                    return;
                }
                switch (args[2].toLowerCase()) {
                    case "legit" -> { LegitProfile.applyLegit(); msg("§aLegit profile"); }
                    case "semi" -> { LegitProfile.applySemi(); msg("§eSemi profile"); }
                    case "rage" -> { LegitProfile.applyRage(); msg("§cRage profile"); }
                    case "scout" -> { LegitProfile.applyScout(); msg("§bScout profile"); }
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
            default -> msg("§cUnknown command");
        }
    }

    private void handleFriend(String[] args) {
        if (args.length < 3) { msg("§ffriend add|del|list"); return; }
        String a = args[2].toLowerCase();
        if (a.equals("list")) {
            msg("§fFriends: §a" + String.join(", ", friendManager.getFriends()));
            return;
        }
        if (args.length < 4) { msg("§cNeed name"); return; }
        if (a.equals("add")) {
            friendManager.add(args[3]);
            msg("§a+ " + args[3]);
            configManager.save();
        } else if (a.equals("del") || a.equals("remove")) {
            friendManager.remove(args[3]);
            msg("§c- " + args[3]);
            configManager.save();
        }
    }

    private void handleConfig(String[] args) {
        if (args.length < 3) { msg("§fconfig save|load"); return; }
        if (args[2].equalsIgnoreCase("save")) {
            configManager.save();
            msg("§aSaved");
        } else if (args[2].equalsIgnoreCase("load")) {
            configManager.load();
            msg("§aLoaded");
        }
    }

    private void msg(String s) {
        var p = net.minecraft.client.MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }
}
