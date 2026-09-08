package com.jay.hackclient.util;

import net.minecraft.util.math.MathHelper;

/**
 * Angle interpolation modes (conceptually similar to LB Linear/Sigmoid smooth).
 * Keeps turns soft for ghost play.
 */
public final class AngleSmooth {

    public enum Mode { LINEAR, SIGMOID }

    private AngleSmooth() {}

    public static float apply(float delta, float factor, Mode mode) {
        factor = MathHelper.clamp(factor, 0.05f, 0.5f);
        if (mode == Mode.SIGMOID) {
            // Ease-in-out style: smaller steps when close, moderate when far
            float t = MathHelper.clamp(Math.abs(delta) / 45f, 0f, 1f);
            float s = t * t * (3f - 2f * t); // smoothstep
            float f = factor * (0.45f + 0.55f * s);
            return delta * f;
        }
        return delta * factor;
    }

    public static float[] stepTowards(
            float currentYaw, float currentPitch,
            float targetYaw, float targetPitch,
            float factor, float maxStep, Mode mode
    ) {
        float dy = MathHelper.wrapDegrees(targetYaw - currentYaw);
        float dp = targetPitch - currentPitch;

        dy = MathHelper.clamp(dy, -maxStep, maxStep);
        dp = MathHelper.clamp(dp, -maxStep * 0.7f, maxStep * 0.7f);

        float outYaw = currentYaw + apply(dy, factor, mode);
        float outPitch = MathHelper.clamp(currentPitch + apply(dp, factor, mode), -90f, 90f);
        return new float[]{outYaw, outPitch};
    }
}
