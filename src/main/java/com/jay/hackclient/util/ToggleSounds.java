package com.jay.hackclient.util;

import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundEvents;

/** Soft UI click on module toggle. */
public final class ToggleSounds {

    private ToggleSounds() {}

    public static void play(boolean enabled) {
        if (!ClientSettings.toggleSounds) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null) return;
        try {
            float pitch = enabled ? 1.15f : 0.85f;
            mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.35f, pitch);
        } catch (Throwable ignored) {}
    }
}
