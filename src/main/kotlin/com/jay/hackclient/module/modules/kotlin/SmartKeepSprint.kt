package com.jay.hackclient.module.modules.kotlin

import com.jay.hackclient.module.Module
import com.jay.hackclient.module.setting.BoolSetting
import com.jay.hackclient.module.setting.NumberSetting

/**
 * Kotlin module — soft keep-sprint after hits (depends on Fabric Language Kotlin).
 */
class SmartKeepSprint : Module(
    "SmartKeepSprint",
    "Kotlin: keep sprint after attack",
    Category.MOVEMENT
) {
    private val onlySword = BoolSetting("OnlySword", "Only when holding weapon", true)
    private val delayMs = NumberSetting("Delay", "Re-sprint delay ms", 80.0, 0.0, 250.0, 10.0)

    private var lastHit = 0L

    init {
        addSetting(onlySword)
        addSetting(delayMs)
    }

    override fun onTick() {
        val p = mc.player ?: return
        if (p.hurtTime <= 0) return
        val now = System.currentTimeMillis()
        if (now - lastHit < delayMs.get()) return
        lastHit = now

        if (onlySword.get()) {
            val item = p.mainHandStack.item
            val name = item.toString().lowercase()
            if (!name.contains("sword") && !name.contains("axe")) return
        }

        if (mc.options.forwardKey.isPressed && p.hungerManager.foodLevel > 6) {
            p.isSprinting = true
        }
    }
}
