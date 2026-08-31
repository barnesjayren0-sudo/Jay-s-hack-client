package com.jay.hackclient.module.modules.kotlin

import com.jay.hackclient.module.Module
import com.jay.hackclient.module.setting.BoolSetting
import com.jay.hackclient.module.setting.NumberSetting
import net.minecraft.util.math.BlockPos

/** Kotlin edge-safeguard while shifting on blocks (pairs with SafeWalk). */
class LegitBridgeAssist : Module(
    "LegitBridgeAssist",
    "Kotlin: soft edge assist when shifting",
    Category.MOVEMENT
) {
    private val onlySneak = BoolSetting("OnlySneak", "Only while sneaking", true)
    private val edgeCheck = NumberSetting("Edge", "Look-ahead blocks", 0.3, 0.1, 0.6, 0.05)

    init {
        addSetting(onlySneak)
        addSetting(edgeCheck)
    }

    override fun onTick() {
        val p = mc.player ?: return
        val w = mc.world ?: return
        if (onlySneak.get() && !p.isSneaking) return
        if (!p.isOnGround) return

        val yaw = Math.toRadians(p.yaw.toDouble())
        val dx = -Math.sin(yaw) * edgeCheck.get()
        val dz = Math.cos(yaw) * edgeCheck.get()
        val feet = p.blockPos
        val ahead = BlockPos.ofFloored(p.x + dx, p.y - 0.1, p.z + dz)
        val aheadDownAir = w.getBlockState(ahead.down()).isAir
        val feetSolid = !w.getBlockState(feet.down()).isAir
        if (aheadDownAir && feetSolid) {
            val v = p.velocity
            p.setVelocity(v.x * 0.55, v.y, v.z * 0.55)
        }
    }
}
