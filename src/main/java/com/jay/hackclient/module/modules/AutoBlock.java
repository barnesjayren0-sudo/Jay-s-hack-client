package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** AutoBlock — raise shield when enemy close. */
public class AutoBlock extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Threat range", 3.6, 2.0, 6.0, 0.1);
    public final BoolSetting onlySword = new BoolSetting("Sword In Hand", "Only with sword/axe", false);
    public final BoolSetting releaseOnAttack = new BoolSetting("Release On Attack", "Stop when attacking", true);

    public AutoBlock() {
        super("AutoBlock", "Auto shield when enemy close", Category.COMBAT);
        addSetting(range); addSetting(onlySword); addSetting(releaseOnAttack);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (releaseOnAttack.get() && mc.options.attackKey.isPressed()) return;

        ItemStack off = mc.player.getOffHandStack();
        ItemStack main = mc.player.getMainHandStack();
        boolean hasShield = off.isOf(Items.SHIELD) || main.isOf(Items.SHIELD);
        if (!hasShield) { setTag(null); return; }

        if (onlySword.get()) {
            String n = main.getItem().toString().toLowerCase();
            if (!n.contains("sword") && !n.contains("axe") && !off.isOf(Items.SHIELD)) return;
        }

        boolean threat = false;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            if (mc.player.distanceTo(p) <= range.get()) { threat = true; break; }
        }
        if (!threat) { setTag(null); return; }

        Hand hand = off.isOf(Items.SHIELD) ? Hand.OFF_HAND : Hand.MAIN_HAND;
        if (!mc.player.isUsingItem()) mc.interactionManager.interactItem(mc.player, hand);
        setTag("block");
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
