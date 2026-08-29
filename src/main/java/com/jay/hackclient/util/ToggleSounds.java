package com.jay.hackclient.util;

import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;

/** Soft UI click sounds on module toggle (premium-client style). */
public final class ToggleSounds {

    private ToggleSounds() {}

    public static void play(boolean enabled) {
        if (!ClientSettings.toggleSounds) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getSoundManager() == null) return;
        try {
            var sound = enabled
                    ? SoundEvents.UI_BUTTON_CLICK.value()
                    : SoundEvents.UI_BUTTON_CLICK.value();
            float pitch = enabled ? 1.15f : 0.85f;
            mc.getSoundManager().play(PositionedSoundInstance.master(sound, pitch, 0.35f));
        } catch (Throwable ignored) {}
    }
}
