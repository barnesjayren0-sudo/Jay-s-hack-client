package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.SlotLock;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Place cobweb under / on target feet. */
public class AutoWeb extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Place range", 4.5, 2.0, 6.0, 0.1);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 150, 50, 400, 10);

    private long last;

    public AutoWeb() {
        super("AutoWeb", "Web under target", Category.COMBAT);
        addSetting(range);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - last < delay.getInt()) return;

        PlayerEntity t = TargetUtil.find(range.get(), 120f);
        if (t == null) return;

        int slot = findWeb();
        if (slot < 0) return;

        BlockPos feet = t.getBlockPos();
        if (!mc.world.getBlockState(feet).isReplaceable()) return;

        if (!SlotLock.tryAcquire("AutoWeb", 200, 15)) return;
        int prev = 0;
        try { prev = mc.player.getInventory().getSelectedSlot(); } catch (Throwable ignored) {}
        try { mc.player.getInventory().setSelectedSlot(slot); } catch (Throwable ignored) {}

        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(feet.down()), Direction.UP, feet.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        try { mc.player.getInventory().setSelectedSlot(prev); } catch (Throwable ignored) {}
        SlotLock.release("AutoWeb");
        last = now;
    }

    private int findWeb() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.COBWEB)) return i;
        }
        return -1;
    }
}
