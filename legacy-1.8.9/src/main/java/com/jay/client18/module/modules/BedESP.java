package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * BedESP — scans only around the player (loaded area), throttled.
 * For BedWars-style maps; not a global radar.
 */
public class BedESP extends Module {

    public int radius = 48;
    public int scanIntervalTicks = 40;

    private final List<BlockPos> beds = new ArrayList<BlockPos>();
    private int tick;
    private boolean registered;

    public BedESP() {
        super("BedESP", "Beds in loaded range", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
        }
        beds.clear();
        tick = 0;
    }

    @Override
    public void onDisable() {
        beds.clear();
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        tick++;
        if (tick % scanIntervalTicks != 0) return;
        scan();
    }

    private void scan() {
        beds.clear();
        BlockPos origin = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        int r = radius;
        // Coarse step to keep mobile cheap
        for (int x = -r; x <= r; x += 1) {
            for (int y = -8; y <= 8; y++) {
                for (int z = -r; z <= r; z += 1) {
                    if (x * x + z * z > r * r) continue;
                    BlockPos p = origin.add(x, y, z);
                    Block b = mc.theWorld.getBlockState(p).getBlock();
                    if (b == Blocks.bed || b instanceof BlockBed) {
                        beds.add(p);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (!isEnabled() || mc.thePlayer == null) return;
        if (mc.gameSettings.hideGUI) return;

        for (BlockPos p : beds) {
            double x = p.getX() - mc.getRenderManager().viewerPosX;
            double y = p.getY() - mc.getRenderManager().viewerPosY;
            double z = p.getZ() - mc.getRenderManager().viewerPosZ;
            AxisAlignedBB bb = new AxisAlignedBB(x, y, z, x + 1, y + 0.6, z + 1);

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GL11.glLineWidth(1.4f);
            GL11.glColor4f(1.0f, 0.3f, 0.4f, 0.7f);
            RenderGlobal.drawSelectionBoundingBox(bb);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
}
