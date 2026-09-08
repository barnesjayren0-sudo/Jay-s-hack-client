package com.jay.client18.mixin;

import com.jay.client18.JayClient18;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Intercept .jay commands. Registered from JayClient18. */
public class ChatHook {

    @SubscribeEvent
    public void onChat(ClientChatEvent e) {
        String msg = e.message;
        if (msg != null && msg.startsWith(".jay")) {
            e.setCanceled(true);
            JayClient18.handleChat(msg);
        }
    }
}
