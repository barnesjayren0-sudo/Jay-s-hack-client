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
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.config.ConfigManager;
import com.jay.hackclient.event.EventBus;
import com.jay.hackclient.event.events.TickEvent;
import com.jay.hackclient.friend.FriendManager;
import com.jay.hackclient.gui.ClickGuiScreen;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.ModuleManager;
import com.jay.hackclient.module.modules.*;
import com.jay.hackclient.profile.LegitProfile;
import com.jay.hackclient.render.HudRenderer;
import com.jay.hackclient.settings.ClientSettings;

public class JayHackClient implements ClientModInitializer {

    public static final String NAME = "Jay's Hack Client";
    public static final String VERSION = "1.11.0";

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;
    public static FriendManager friendManager;
    public static ConfigManager configManager;
    public static EventBus EVENT_BUS;

    public static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("jayhackclient", "main"));

    private static KeyBinding menuKey;
    private static KeyBinding panicKey;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        EVENT_BUS = new EventBus();
        moduleManager = new ModuleManager();
        friendManager = new FriendManager();
        configManager = new ConfigManager();

        ClientSettings.applySwordConfig(); // quiet default

        moduleManager.register(new KillAura());
        moduleManager.register(new AimAssist());
        moduleManager.register(new TriggerBot());
        moduleManager.register(new AutoClicker());
        moduleManager.register(new AutoSword());
        moduleManager.register(new ShieldBreak());
        moduleManager.register(new AutoBlock());
        moduleManager.register(new JumpReset());
        moduleManager.register(new Criticals());
        moduleManager.register(new Velocity());
        moduleManager.register(new WTap());
        moduleManager.register(new Reach());
        moduleManager.register(new Hitboxes());
        moduleManager.register(new AutoPot());
        moduleManager.register(new AnchorMacro());
        moduleManager.register(new AntiBot());

        moduleManager.register(new AutoSprint());
        moduleManager.register(new NoSlow());
        moduleManager.register(new Speed());
        moduleManager.register(new NoFall());

        moduleManager.register(new ESP());
        moduleManager.register(new Nametags());
        moduleManager.register(new FullBright());
        moduleManager.register(new StorageESP());
        moduleManager.register(new TargetHUD());
        moduleManager.register(new HUD());

        moduleManager.register(new AutoArmor());
        moduleManager.register(new AutoTotem());
        moduleManager.register(new OffhandGap());
        moduleManager.register(new Refill());
        moduleManager.register(new AutoGap());
        moduleManager.register(new AutoHead());
        moduleManager.register(new PearlCatch());

        moduleManager.register(new BaseFinder());
        moduleManager.register(new SpawnerFinder());
        moduleManager.register(new PlayerRadar());
        moduleManager.register(new PortalFinder());
        moduleManager.register(new PathToBase());

        Module hud = moduleManager.getModuleByName("HUD");
        if (hud != null) hud.setEnabled(true);
        Module ab = moduleManager.getModuleByName("AntiBot");
        if (ab != null) ab.setEnabled(true);

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.clickgui", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.panic", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DELETE, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            EVENT_BUS.post(TickEvent.INSTANCE);
            if (panicKey.wasPressed()) {
                moduleManager.panic();
                client.player.sendMessage(Text.literal("§8[§cPANIC§8] §fAll off"), false);
            }
            if (menuKey.wasPressed()) {
                if (client.currentScreen instanceof ClickGuiScreen) client.setScreen(null);
                else if (client.currentScreen == null) client.setScreen(new ClickGuiScreen());
            }
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
        System.out.println("[" + NAME + "] v" + VERSION + " " + ClientSettings.summarize());
    }

    private void toggle(String name) {
        if (moduleManager.isFrozen()) { msg("§cUnpanic first"); return; }
        Module m = moduleManager.getModuleByName(name);
        if (m != null) m.toggle();
    }

    private void handleCommand(String message) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            msg("§fgui profile sword nethpot settings set panic");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "gui", "menu", "list", "help" -> client.setScreen(new ClickGuiScreen());
            case "toggle" -> { if (args.length >= 3) toggle(args[2]); }
            case "binds", "keys" -> {
                msg("§7J Aim §7T Trigger §7R Aura §7V ShieldBreak");
                msg("§7G Sprint §7RShift GUI §7Del Panic");
            }
            case "off", "disableall" -> { moduleManager.disableAll(); msg("§eOff"); }
            case "panic" -> { moduleManager.panic(); msg("§cPANIC"); }
            case "unpanic", "unfreeze" -> { moduleManager.unfreeze(); msg("§aOK"); }
            case "profile" -> { if (args.length >= 3) applyProfile(args[2].toLowerCase()); }
            case "sword" -> { LegitProfile.applySword(); msg("§aSword config"); }
            case "nethpot", "pot" -> { LegitProfile.applyNethpot(); msg("§dNethpot config"); }
            case "kit", "smp" -> { LegitProfile.applyKit(); msg("§aKit"); }
            case "crystal" -> { LegitProfile.applyCrystal(); msg("§bCrystal"); }
            case "uhc" -> { LegitProfile.applyUhc(); msg("§6UHC"); }
            case "settings", "cfginfo", "configinfo" -> msg("§f" + ClientSettings.summarize());
            case "set" -> handleSet(args);
            case "path", "baritone" -> {
                msg(BaritoneCompat.isPresent() ? "§aBaritone" : "§cNo Baritone");
                toggle("PathToBase");
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
            default -> msg("§c?");
        }
    }

    private void handleSet(String[] args) {
        if (args.length < 4) {
            msg("§f.set aimrange|aimfov|aimsmooth|aurarange|hitbox|vel <n>");
            return;
        }
        String key = args[2].toLowerCase();
        try {
            double v = Double.parseDouble(args[3]);
            switch (key) {
                case "aimrange" -> ClientSettings.aimRange = clamp(v, 2, 6);
                case "aimfov" -> ClientSettings.aimFov = (float) clamp(v, 30, 180);
                case "aimsmooth" -> ClientSettings.aimSmooth = (float) clamp(v, 0.1, 0.8);
                case "aurarange" -> ClientSettings.auraRange = clamp(v, 2.5, 4.5);
                case "hitbox", "hb" -> {
                    ClientSettings.hitboxExpand = clamp(v, 0, 0.4);
                    Hitboxes.setExpand(ClientSettings.hitboxExpand);
                }
                case "vel", "velocity" -> ClientSettings.velocityFactor = clamp(v, 0.4, 0.95);
                case "miss" -> ClientSettings.missChance = (int) clamp(v, 0, 20);
                default -> { msg("§cUnknown setting"); return; }
            }
            msg("§a" + key + " = " + v);
        } catch (NumberFormatException e) {
            msg("§cNumber required");
        }
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private void applyProfile(String name) {
        switch (name) {
            case "legit" -> { LegitProfile.applyLegit(); msg("§aLegit"); }
            case "semi" -> { LegitProfile.applySemi(); msg("§eSemi"); }
            case "sword" -> { LegitProfile.applySword(); msg("§aSword"); }
            case "rage" -> { LegitProfile.applyRage(); msg("§cRage"); }
            case "scout" -> { LegitProfile.applyScout(); msg("§bScout"); }
            case "nethpot", "pot" -> { LegitProfile.applyNethpot(); msg("§dNethpot"); }
            case "uhc" -> { LegitProfile.applyUhc(); msg("§6UHC"); }
            case "kit", "smp" -> { LegitProfile.applyKit(); msg("§aKit"); }
            case "crystal" -> { LegitProfile.applyCrystal(); msg("§bCrystal"); }
            default -> msg("§c?");
        }
    }

    private void handleFriend(String[] args) {
        if (args.length < 3) return;
        String a = args[2].toLowerCase();
        if (a.equals("list")) { msg("§f" + String.join(", ", friendManager.getFriends())); return; }
        if (args.length < 4) return;
        if (a.equals("add")) { friendManager.add(args[3]); configManager.save(); msg("§a+ " + args[3]); }
        else if (a.equals("del") || a.equals("remove")) { friendManager.remove(args[3]); configManager.save(); msg("§c- " + args[3]); }
    }

    private void handleConfig(String[] args) {
        if (args.length < 3) return;
        if (args[2].equalsIgnoreCase("save")) { configManager.save(); msg("§aSaved"); }
        else if (args[2].equalsIgnoreCase("load")) { configManager.load(); msg("§aLoaded"); }
    }

    private void msg(String s) {
        var p = MinecraftClient.getInstance().player;
        if (p != null) p.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
    }
}
