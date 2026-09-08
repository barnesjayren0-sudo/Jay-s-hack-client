package com.jay.client18;

import com.jay.client18.config.ConfigManager;
import com.jay.client18.gui.ClickGuiScreen;
import com.jay.client18.module.Module;
import com.jay.client18.module.ModuleManager;
import com.jay.client18.module.modules.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = JayClient18.MODID, name = JayClient18.NAME, version = JayClient18.VERSION, clientSideOnly = true)
public class JayClient18 {

    public static final String MODID = "jayclient18";
    public static final String NAME = "Jay Client 1.8.9";
    public static final String VERSION = "0.1.0";

    public static JayClient18 INSTANCE;
    public static ModuleManager moduleManager;
    public static ConfigManager configManager;

    private boolean wasRightShift;
    private boolean wasDelete;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        INSTANCE = this;
        moduleManager = new ModuleManager();
        configManager = new ConfigManager();

        moduleManager.register(new AimAssist());
        moduleManager.register(new TriggerBot());
        moduleManager.register(new Velocity());
        moduleManager.register(new AutoClicker());
        moduleManager.register(new AutoSword());
        moduleManager.register(new WTap());
        moduleManager.register(new AutoSprint());
        moduleManager.register(new SafeWalk());
        moduleManager.register(new Scaffold());
        moduleManager.register(new ESP());
        moduleManager.register(new FullBright());
        moduleManager.register(new ArrayListMod());
        moduleManager.register(new MiddleClickFriend());

        configManager.load();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new com.jay.client18.render.HudRenderer());
        System.out.println("[" + NAME + "] v" + VERSION + " loaded");
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        // GUI key
        boolean rs = Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        if (rs && !wasRightShift && mc.currentScreen == null) {
            mc.displayGuiScreen(new ClickGuiScreen());
        }
        wasRightShift = rs;

        // Panic
        boolean del = Keyboard.isKeyDown(Keyboard.KEY_DELETE);
        if (del && !wasDelete) {
            moduleManager.panic();
            if (configManager != null) configManager.save();
            msg("§cPANIC §7— all modules off");
        }
        wasDelete = del;

        moduleManager.onTick();
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        // module keybinds polled in ModuleManager
    }

    public static void handleChat(String message) {
        if (!message.startsWith(".jay")) return;
        String[] a = message.trim().split("\\s+");
        if (a.length < 2 || a[1].equalsIgnoreCase("help")) {
            msg("§f.jay gui|panic|friend add/del/list");
            return;
        }
        switch (a[1].toLowerCase()) {
            case "gui":
                Minecraft.getMinecraft().displayGuiScreen(new ClickGuiScreen());
                break;
            case "panic":
                moduleManager.panic();
                configManager.save();
                msg("§cPANIC");
                break;
            case "friend":
                handleFriend(a);
                break;
            default:
                msg("§cUnknown · .jay help");
        }
    }

    private static void handleFriend(String[] a) {
        if (a.length < 3) {
            msg("§f.jay friend add|del|list");
            return;
        }
        if (a[2].equalsIgnoreCase("list")) {
            msg("§f" + String.join(", ", moduleManager.getFriends()));
            return;
        }
        if (a.length < 4) return;
        if (a[2].equalsIgnoreCase("add")) {
            moduleManager.addFriend(a[3]);
            configManager.save();
            msg("§a+ " + a[3]);
        } else if (a[2].equalsIgnoreCase("del") || a[2].equalsIgnoreCase("remove")) {
            moduleManager.removeFriend(a[3]);
            configManager.save();
            msg("§c- " + a[3]);
        }
    }

    public static void msg(String s) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§8[§bJay§8] " + s));
        }
    }

    public static void toggle(String name) {
        Module m = moduleManager.get(name);
        if (m != null) {
            m.toggle();
            configManager.save();
        }
    }
}
