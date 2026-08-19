package com.jay.hackclient.util

/**
 * Small Kotlin helpers callable from Java.
 */
object KotlinHooks {

    @JvmStatic
    fun clamp(v: Double, min: Double, max: Double): Double =
        v.coerceIn(min, max)

    @JvmStatic
    fun lerp(a: Float, b: Float, t: Float): Float =
        a + (b - a) * t.coerceIn(0f, 1f)

    @JvmStatic
    fun formatCombo(n: Int): String =
        if (n <= 0) "" else "§d${n}x"
}
