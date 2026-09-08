package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

/** Quiet player boxes — friends skipped, limited range. */
public class ESP extends Module {

    public double range = 48;
    private boolean registered;

    public ESP() {
        super("ESP", "Player boxes", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (!registered) {
            MinecraftForge.EVENT_BUS.register(this);
            registered = true;
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (!isEnabled() || mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.gameSettings.hideGUI) return;

        for (EntityPlayer p : CombatUtil.playersInRange(range)) {
            if (CombatUtil.isFriend(p)) continue;
            drawBox(p, e.partialTicks);
        }
    }

    private void drawBox(EntityPlayer p, float pt) {
        double x = p.lastTickPosX + (p.posX - p.lastTickPosX) * pt - mc.getRenderManager().viewerPosX;
        double y = p.lastTickPosY + (p.posY - p.lastTickPosY) * pt - mc.getRenderManager().viewerPosY;
        double z = p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * pt - mc.getRenderManager().viewerPosZ;

        AxisAlignedBB bb = new AxisAlignedBB(
                x - p.width / 2, y, z - p.width / 2,
                x + p.width / 2, y + p.height, z + p.width / 2
        );

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GL11.glLineWidth(1.2f);
        GL11.glColor4f(0.24f, 0.86f, 1.0f, 0.55f);
        RenderGlobal.drawSelectionBoundingBox(bb);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
