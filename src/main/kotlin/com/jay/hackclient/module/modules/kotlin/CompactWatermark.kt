package com.jay.hackclient.module.modules.kotlin

import com.jay.hackclient.JayHackClient
import com.jay.hackclient.module.Module
import com.jay.hackclient.module.setting.BoolSetting

/** Kotlin watermark flags used by HUD renderer. */
class CompactWatermark : Module(
    "CompactWatermark",
    "Kotlin: smaller watermark / active count",
    Category.RENDER
) {
    val showFps = BoolSetting("ShowFPS", "Show FPS in watermark", true)
    val showBps = BoolSetting("ShowBPS", "Show blocks/sec", false)

    init {
        addSetting(showFps)
        addSetting(showBps)
        // On by default for mobile
        setEnabled(true)
    }

    override fun onTick() {
        // Flags only — HUD reads settings
    }

    companion object {
        @JvmStatic
        fun isCompact(): Boolean {
            val mm = JayHackClient.moduleManager ?: return true
            val m = mm.getModuleByName("CompactWatermark")
            return m == null || m.isEnabled
        }
    }
}
