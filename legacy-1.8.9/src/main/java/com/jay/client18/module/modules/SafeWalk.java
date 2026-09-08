package com.jay.client18.module.modules;

import com.jay.client18.JayClient18;
import com.jay.client18.module.Module;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;

/** Non-sticky edge sneak — disabled while Scaffold is on. */
public class SafeWalk extends Module {

    private boolean forced;
    private int ticks;

    public SafeWalk() {
        super("SafeWalk", "Sneak on edges", Category.MOVEMENT);
    }

    @Override
    public void onDisable() {
        clear();
    }

    private void clear() {
        if (forced) {
            mc.gameSettings.keyBindSneak.pressed = false;
            forced = false;
            ticks = 0;
        }
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        Module sc = JayClient18.moduleManager.get("Scaffold");
        if (sc != null && sc.isEnabled()) {
            clear();
            return;
        }
        if (!mc.thePlayer.onGround) {
            clear();
            return;
        }

        BlockPos below = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.2, mc.thePlayer.posZ);
        boolean edge = false;
        double mx = mc.thePlayer.motionX;
        double mz = mc.thePlayer.motionZ;
        int sdx = mx > 0.02 ? 1 : (mx < -0.02 ? -1 : 0);
        int sdz = mz > 0.02 ? 1 : (mz < -0.02 ? -1 : 0);
        if (sdx != 0 || sdz != 0) {
            BlockPos ahead = below.add(sdx, 0, sdz);
            if (mc.theWorld.isAirBlock(ahead)) edge = true;
        }

        if (edge) {
            mc.gameSettings.keyBindSneak.pressed = true;
            forced = true;
            ticks++;
            if (ticks > 3) clear();
        } else {
            clear();
        }
    }
}
