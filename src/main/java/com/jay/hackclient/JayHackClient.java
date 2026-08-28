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

import com.jay.hackclient.compat.BaritoneCommands;
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
    public static final String VERSION = "1.23.0";

    public static JayHackClient INSTANCE;
    public static ModuleManager moduleManager;
    public static FriendManager friendManager;
    public static ConfigManager configManager;
    public static EventBus EVENT_BUS;

    public static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("jayhackclient", "main"));

    private static KeyBinding menuKey;
    private static KeyBinding panicKey;
    private static KeyBinding profileKey;

    private static boolean wasAlive = true;
    private static int lastWorldHash = 0;

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        EVENT_BUS = new EventBus();
        moduleManager = new ModuleManager();
        friendManager = new FriendManager();
        configManager = new ConfigManager();

        ClientSettings.applySwordConfig();

        moduleManager.register(new KillAura());
        moduleManager.register(new AimAssist());
        moduleManager.register(new TriggerBot());
        moduleManager.register(new CritAssist());
        moduleManager.register(new AutoClicker());
        moduleManager.register(new AutoSword());
        moduleManager.register(new ShieldBreak());
        moduleManager.register(new AutoBlock());
        moduleManager.register(new JumpReset());
        moduleManager.register(new NoJumpDelay());
        moduleManager.register(new Criticals());
        moduleManager.register(new Velocity());
        moduleManager.register(new WTap());
        moduleManager.register(new STap());
        moduleManager.register(new Reach());
        moduleManager.register(new Hitboxes());
        moduleManager.register(new AutoPot());
        moduleManager.register(new PotRefill());
        moduleManager.register(new AnchorMacro());
        moduleManager.register(new AntiBot());

        moduleManager.register(new AutoSprint());
        moduleManager.register(new NoSlow());
        moduleManager.register(new Speed());
        moduleManager.register(new NoFall());
        moduleManager.register(new SafeWalk());

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
        moduleManager.register(new PearlAssist());
        moduleManager.register(new InvManager());

        // World / find — loaded chunks only
        moduleManager.register(new BaseFinder());
        moduleManager.register(new StorageFinder());
        moduleManager.register(new BuildFinder());
        moduleManager.register(new BeaconFinder());
        moduleManager.register(new SpawnerFinder());
        moduleManager.register(new PortalFinder());
        moduleManager.register(new PlayerRadar());
        moduleManager.register(new PathToBase());
        moduleManager.register(new BaritoneControl());
        moduleManager.register(new Scaffold());
        moduleManager.register(new AutoTool());

        moduleManager.register(new MiddleClickFriend());

        Module hud = moduleManager.getModuleByName("HUD");
        if (hud != null) hud.setEnabled(true);
        Module ab = moduleManager.getModuleByName("AntiBot");
        if (ab != null) ab.setEnabled(true);
        Module mcf = moduleManager.getModuleByName("MiddleClickFriend");
        if (mcf != null) mcf.setEnabled(true);

        menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.clickgui", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, CATEGORY));
        panicKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.panic", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_DELETE, CATEGORY));
        profileKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jayhackclient.profile", InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_P, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                if (lastWorldHash != 0) {
                    moduleManager.disableCombat();
                    lastWorldHash = 0;
                    wasAlive = true;
                }
                return;
            }

            int wh = System.identityHashCode(client.world);
            if (lastWorldHash != 0 && wh != lastWorldHash) moduleManager.disableCombat();
            lastWorldHash = wh;

            boolean alive = client.player.isAlive() && client.player.getHealth() > 0;
            if (wasAlive && !alive) {
                moduleManager.disableCombat();
                BaritoneCompat.cancel();
                if (configManager != null) configManager.save();
                client.player.sendMessage(Text.literal("§8[§bJay§8] §7Combat off (death)"), false);
            }
            wasAlive = alive;

            EVENT_BUS.post(TickEvent.INSTANCE);
            if (panicKey.wasPressed()) {
                moduleManager.panic();
                BaritoneCompat.cancel();
                if (configManager != null) configManager.save();
                client.player.sendMessage(Text.literal("§8[§cPANIC§8] §fAll off"), false);
            }
            if (profileKey.wasPressed()) cycleProfile();
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
            if (BaritoneCommands.tryHandle(message)) return false;
            if (message.startsWith(".jay")) {
                handleCommand(message);
                return false;
            }
            return true;
        });

        configManager.load();
        System.out.println("[" + NAME + "] v" + VERSION + " Baritone=" + BaritoneCompat.isPresent());
    }

    private void cycleProfile() {
        ClientSettings.profileCycleIndex =
                (ClientSettings.profileCycleIndex + 1) % ClientSettings.PROFILE_CYCLE.length;
        String name = ClientSettings.PROFILE_CYCLE[ClientSettings.profileCycleIndex];
        applyProfile(name);
        if (configManager != null) configManager.save();
        msg("§aProfile §f" + name);
    }

    private void toggle(String name) {
        if (moduleManager.isFrozen()) { msg("§cUnpanic"); return; }
        Module m = moduleManager.getModuleByName(name);
        if (m != null) {
            m.toggle();
            if (configManager != null) configManager.save();
        }
    }

    private void handleCommand(String message) {
        var client = MinecraftClient.getInstance();
        if (client.player == null) return;
        String[] args = message.trim().split("\\s+");
        if (args.length < 2) {
            msg("§f.gui .scan .radar .storage .build .path .goto x y z");
            return;
        }

        switch (args[1].toLowerCase()) {
            case "gui", "menu" -> client.setScreen(new ClickGuiScreen());
            case "toggle" -> { if (args.length >= 3) toggle(args[2]); }
            case "sword" -> { LegitProfile.applySword(); configManager.save(); msg("§aSword"); }
            case "swordaggro", "aggro" -> { LegitProfile.applySwordAggressive(); configManager.save(); msg("§6Aggro"); }
            case "nethpot", "pot" -> { LegitProfile.applyNethpot(); configManager.save(); msg("§dNethpot"); }
            case "kit" -> { LegitProfile.applyKit(); configManager.save(); msg("§aKit"); }
            case "profile" -> {
                if (args.length >= 3) { applyProfile(args[2]); configManager.save(); }
                else cycleProfile();
            }
            case "fav", "favorite" -> {
                if (args.length < 3) { msg("§f" + String.join(", ", ClientSettings.favorites)); return; }
                ClientSettings.toggleFavorite(args[2]);
                configManager.save();
                msg((ClientSettings.isFavorite(args[2]) ? "§a★ " : "§7☆ ") + args[2]);
            }
            case "settings" -> msg("§f" + ClientSettings.summarize());
            case "velmode", "velocity" -> {
                if (args.length < 3) { msg("§fsoft|medium|strong"); return; }
                ClientSettings.applyVelocityMode(args[2]);
                configManager.save();
                msg("§aVel " + ClientSettings.velocityMode);
            }
            case "aimmode" -> {
                if (args.length < 3) return;
                ClientSettings.aimMode = args[2].equalsIgnoreCase("silent") ? "silent" : "classic";
                configManager.save();
                msg("§a" + ClientSettings.aimMode);
            }
            case "priority", "prio" -> {
                if (args.length < 3) return;
                ClientSettings.targetPriority = args[2].toLowerCase();
                configManager.save();
                msg("§a" + ClientSettings.targetPriority);
            }
            case "set" -> handleSet(args);
            case "friend", "friends" -> handleFriend(args);
            case "config", "cfg" -> handleConfig(args);
            case "off" -> { moduleManager.disableAll(); BaritoneCompat.cancel(); configManager.save(); msg("§eOff"); }
            case "panic" -> { moduleManager.panic(); BaritoneCompat.cancel(); configManager.save(); msg("§cPANIC"); }
            case "unpanic" -> { moduleManager.unfreeze(); msg("§aOK"); }

            // Find / path
            case "scan", "base" -> {
                Module bf = moduleManager.getModuleByName("BaseFinder");
                if (bf instanceof BaseFinder f) f.scan(true);
            }
            case "radar", "players" -> {
                Module pr = moduleManager.getModuleByName("PlayerRadar");
                if (pr instanceof PlayerRadar r) r.report(true);
            }
            case "storage", "store" -> {
                Module sf = moduleManager.getModuleByName("StorageFinder");
                if (sf instanceof StorageFinder s) s.scan(true);
            }
            case "build" -> {
                Module bf = moduleManager.getModuleByName("BuildFinder");
                if (bf instanceof BuildFinder b) b.scan(true);
            }
            case "path" -> PathToBase.runPath(PathToBase.lastTarget);
            case "goto", "tp" -> {
                if (args.length < 5) { msg("§f.jay goto <x> <y> <z>"); return; }
                try {
                    int x = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);
                    int z = Integer.parseInt(args[4]);
                    PathToBase.runPath(x, y, z);
                } catch (NumberFormatException e) {
                    msg("§cNumbers only");
                }
            }
            case "stoppath", "cancel" -> {
                BaritoneCompat.cancel();
                msg("§ePath cancelled");
            }

            case "baritone", "b" -> {
                if (args.length >= 3) {
                    StringBuilder sb = new StringBuilder("#");
                    for (int i = 2; i < args.length; i++) {
                        if (i > 2) sb.append(' ');
                        sb.append(args[i]);
                    }
                    BaritoneCommands.tryHandle(sb.toString());
                } else {
                    msg("§f" + BaritoneCompat.status());
                    toggle("Baritone");
                }
            }
            case "binds" -> msg("§7RShift GUI · .scan .radar .storage .build .path .goto x y z");
            default -> msg("§c?");
        }
    }

    private void handleSet(String[] args) {
        if (args.length < 4) {
            msg("§f.set velh|aimrange|miss|hitbox|reach <n>");
            return;
        }
        try {
            double v = Double.parseDouble(args[3]);
            switch (args[2].toLowerCase()) {
                case "velh", "vel" -> ClientSettings.velocityHorizontal = Math.max(0.4, Math.min(0.95, v));
                case "velv" -> ClientSettings.velocityVertical = Math.max(0.65, Math.min(1.0, v));
                case "aimrange" -> ClientSettings.aimRange = v;
                case "aimfov" -> ClientSettings.aimFov = (float) v;
                case "aimsmooth" -> ClientSettings.aimSmooth = (float) v;
                case "hitbox", "hb" -> Hitboxes.setExpand(v);
                case "reach" -> Reach.setReach(v);
                case "miss" -> ClientSettings.missChance = (int) v;
                case "potmin" -> ClientSettings.potSlotMin = Math.max(0, Math.min(8, (int) v));
                case "potmax" -> ClientSettings.potSlotMax = Math.max(0, Math.min(8, (int) v));
                default -> { msg("§cUnknown"); return; }
            }
            configManager.save();
            msg("§a" + args[2] + "=" + v);
        } catch (Exception e) { msg("§cNumber?"); }
    }

    private void applyProfile(String name) {
        switch (name.toLowerCase()) {
            case "legit" -> LegitProfile.applyLegit();
            case "semi" -> LegitProfile.applySemi();
            case "sword" -> LegitProfile.applySword();
            case "swordaggro", "aggro" -> LegitProfile.applySwordAggressive();
            case "rage" -> LegitProfile.applyRage();
            case "nethpot" -> LegitProfile.applyNethpot();
            case "uhc" -> LegitProfile.applyUhc();
            case "kit" -> LegitProfile.applyKit();
            case "crystal" -> LegitProfile.applyCrystal();
            case "scout" -> LegitProfile.applyScout();
            default -> { msg("§c?"); return; }
        }
        ClientSettings.lastProfile = name.toLowerCase();
        msg("§a" + name);
    }

    private void handleFriend(String[] args) {
        if (args.length < 3) { msg("§f.friend add|del|list"); return; }
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
