package com.jay.hackclient.config

import com.jay.hackclient.JayHackClient
import com.jay.hackclient.settings.ClientSettings
import me.shedaniel.clothconfig2.api.ConfigBuilder
import me.shedaniel.clothconfig2.api.ConfigCategory
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

/**
 * Real Cloth Config usage — client settings screen (Mod Menu / .jay cloth).
 */
object JayClothConfig {

    @JvmStatic
    fun createScreen(parent: Screen?): Screen {
        val builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Text.literal("Jay Client Settings"))
            .setSavingRunnable {
                try {
                    JayHackClient.configManager?.save()
                } catch (_: Throwable) {
                }
            }

        val eb: ConfigEntryBuilder = builder.entryBuilder()

        val combat: ConfigCategory = builder.getOrCreateCategory(Text.literal("Combat"))
        combat.addEntry(
            eb.startDoubleField(Text.literal("Aim range"), ClientSettings.aimRange)
                .setMin(2.5).setMax(6.0)
                .setSaveConsumer { ClientSettings.aimRange = it }
                .build()
        )
        combat.addEntry(
            eb.startFloatField(Text.literal("Aim FOV"), ClientSettings.aimFov)
                .setMin(20f).setMax(180f)
                .setSaveConsumer { ClientSettings.aimFov = it }
                .build()
        )
        combat.addEntry(
            eb.startFloatField(Text.literal("Aim smooth"), ClientSettings.aimSmooth)
                .setMin(0.05f).setMax(1.0f)
                .setSaveConsumer { ClientSettings.aimSmooth = it }
                .build()
        )
        combat.addEntry(
            eb.startDoubleField(Text.literal("Aura range"), ClientSettings.auraRange)
                .setMin(2.5).setMax(6.0)
                .setSaveConsumer { ClientSettings.auraRange = it }
                .build()
        )
        combat.addEntry(
            eb.startDoubleField(Text.literal("Velocity H"), ClientSettings.velocityHorizontal)
                .setMin(0.2).setMax(1.0)
                .setSaveConsumer { ClientSettings.velocityHorizontal = it }
                .build()
        )

        val ui: ConfigCategory = builder.getOrCreateCategory(Text.literal("UI"))
        ui.addEntry(
            eb.startBooleanToggle(Text.literal("Toggle sounds"), ClientSettings.toggleSounds)
                .setSaveConsumer { ClientSettings.toggleSounds = it }
                .build()
        )
        ui.addEntry(
            eb.startBooleanToggle(Text.literal("Show active count"), ClientSettings.showActiveCount)
                .setSaveConsumer { ClientSettings.showActiveCount = it }
                .build()
        )
        ui.addEntry(
            eb.startBooleanToggle(Text.literal("Hide HUD on screenshot"), ClientSettings.hideHudOnScreenshot)
                .setSaveConsumer { ClientSettings.hideHudOnScreenshot = it }
                .build()
        )

        val misc: ConfigCategory = builder.getOrCreateCategory(Text.literal("Misc"))
        misc.addEntry(
            eb.startBooleanToggle(Text.literal("Ping-scale delays"), ClientSettings.pingScaleDelays)
                .setSaveConsumer { ClientSettings.pingScaleDelays = it }
                .build()
        )
        misc.addEntry(
            eb.startStrField(Text.literal("Aim mode"), ClientSettings.aimMode)
                .setSaveConsumer { ClientSettings.aimMode = it }
                .build()
        )
        misc.addEntry(
            eb.startStrField(Text.literal("Target priority"), ClientSettings.targetPriority)
                .setSaveConsumer { ClientSettings.targetPriority = it }
                .build()
        )

        return builder.build()
    }
}
