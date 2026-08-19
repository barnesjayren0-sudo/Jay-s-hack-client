package com.jay.hackclient.module.modules

import com.jay.hackclient.module.Module
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult

/**
 * Kotlin module — tracks sword combo hits and sprint-resets timing hints.
 * Shows combo count in module name style via chat every few hits (optional light).
 */
class ComboAssist : Module("ComboAssist", "Tracks hit combos (Kotlin)", Category.COMBAT) {

    private var combo = 0
    private var lastTargetId: Int = -1
    private var lastHitMs: Long = 0
    private var lastSwing = 0

    override fun onTick() {
        val player = mc.player ?: return
        val now = System.currentTimeMillis()

        // Reset combo if too long since last hit
        if (combo > 0 && now - lastHitMs > 1200) {
            combo = 0
            lastTargetId = -1
        }

        val swing = player.handSwingTicks
        if (swing == 1 && lastSwing != 1) {
            val target = crosshairPlayer()
            if (target != null) {
                if (target.id == lastTargetId || lastTargetId == -1) {
                    combo++
                } else {
                    combo = 1
                }
                lastTargetId = target.id
                lastHitMs = now
                currentCombo = combo
            }
        }
        lastSwing = swing
    }

    override fun onDisable() {
        combo = 0
        currentCombo = 0
        lastTargetId = -1
    }

    private fun crosshairPlayer(): PlayerEntity? {
        val hit = mc.crosshairTarget ?: return null
        if (hit.type != HitResult.Type.ENTITY) return null
        val ehr = hit as? EntityHitResult ?: return null
        val e = ehr.entity
        return if (e is PlayerEntity && e != mc.player) e else null
    }

    companion object {
        /** Read by HUD / other modules */
        @JvmField
        var currentCombo: Int = 0
    }
}
