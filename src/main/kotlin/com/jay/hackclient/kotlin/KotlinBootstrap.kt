package com.jay.hackclient.kotlin

import com.jay.hackclient.module.ModuleManager
import com.jay.hackclient.module.modules.kotlin.CompactWatermark
import com.jay.hackclient.module.modules.kotlin.LegitBridgeAssist
import com.jay.hackclient.module.modules.kotlin.SmartKeepSprint

/**
 * Registers modules written in Kotlin (Fabric Language Kotlin).
 * Called from Java [com.jay.hackclient.ModuleBootstrap].
 */
object KotlinBootstrap {
    @JvmStatic
    fun register(mm: ModuleManager?) {
        if (mm == null) return
        try {
            mm.register(SmartKeepSprint())
            mm.register(LegitBridgeAssist())
            mm.register(CompactWatermark())
        } catch (t: Throwable) {
            System.err.println("[Jay] Kotlin modules: ${t.message}")
        }
    }
}
