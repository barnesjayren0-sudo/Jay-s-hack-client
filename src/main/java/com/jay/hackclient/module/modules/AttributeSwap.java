package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

/**
 * AttributeSwap — Simple / Smart weapon attribute swap.
 * Smart scans hotbar for Sword/Axe/Mace and swaps with real UpdateSelectedSlotC2SPacket.
 */
public class AttributeSwap extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Swap style", "Smart", "Simple", "Smart");
    public final ModeSetting weapon = new ModeSetting("Weapon", "Smart: preferred type", "Sword", "Sword", "Axe", "Mace", "Any");
    public final NumberSetting swapSlot = new NumberSetting("Swap Slot", "Simple: hotbar 0-8", 1, 0, 8, 1);
    public final NumberSetting holdMs = new NumberSetting("Hold Ms", "Ms before restore", 50, 0, 200, 5);
    public final BoolSetting onlyOnAttack = new BoolSetting("Only On Attack", "Swap only on attack", true);
    public final BoolSetting restore = new BoolSetting("Restore", "Switch back after hit", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "UpdateSelectedSlotC2SPacket", true);

    private int originalSlot = -1;
    private long swapUntil;
    private boolean swapped;

    public AttributeSwap() {
        super("AttributeSwap", "Simple / Smart attribute weapon swap", Category.COMBAT);
        addSetting(mode); addSetting(weapon); addSetting(swapSlot); addSetting(holdMs);
        addSetting(onlyOnAttack); addSetting(restore); addSetting(realPackets);
    }

    @Override
    public void onDisable() {
        if (swapped && originalSlot >= 0) select(originalSlot);
        swapped = false; originalSlot = -1; setTag(null);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (swapped && System.currentTimeMillis() >= swapUntil) {
            if (restore.get() && originalSlot >= 0) select(originalSlot);
            swapped = false; originalSlot = -1; setTag(null); return;
        }
        if (onlyOnAttack.get() && !mc.options.attackKey.isPressed()) return;
        if (!swapped) trySwapForAttack(null);
    }

    public boolean trySwapForAttack(Entity target) {
        if (!isEnabled() || mc.player == null) return false;
        if (swapped) return true;
        int current = mc.player.getInventory().selectedSlot;
        int dest = -1;
        if ("Simple".equals(mode.get())) {
            dest = (int) swapSlot.get();
            if (dest == current) return false;
            if (mc.player.getInventory().getStack(dest).isEmpty()) return false;
        } else {
            dest = findSmartSlot(current);
            if (dest < 0 || dest == current) return false;
        }
        originalSlot = current;
        select(dest);
        swapped = true;
        swapUntil = System.currentTimeMillis() + (long) holdMs.get();
        setTag(mode.get() + " ->" + dest);
        return true;
    }

    private int findSmartSlot(int current) {
        String pref = weapon.get().toLowerCase();
        int best = -1, bestScore = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            int score = scoreItem(stack.getItem().toString().toLowerCase(), pref);
            if (score > bestScore) { bestScore = score; best = i; }
        }
        if (best == current && bestScore > 0) {
            for (int i = 0; i < 9; i++) {
                if (i == current) continue;
                ItemStack stack = mc.player.getInventory().getStack(i);
                if (stack.isEmpty()) continue;
                if (scoreItem(stack.getItem().toString().toLowerCase(), pref) == bestScore) return i;
            }
        }
        return bestScore > 0 ? best : -1;
    }

    private int scoreItem(String name, String pref) {
        if ("any".equals(pref)) {
            if (name.contains("mace")) return 35;
            if (name.contains("sword")) return 30;
            if (name.contains("axe") && !name.contains("pickaxe")) return 28;
            return 0;
        }
        if ("sword".equals(pref) && name.contains("sword")) {
            if (name.contains("netherite")) return 50;
            if (name.contains("diamond")) return 40;
            if (name.contains("iron")) return 30;
            return 20;
        }
        if ("axe".equals(pref) && name.contains("axe") && !name.contains("pickaxe")) {
            if (name.contains("netherite")) return 50;
            if (name.contains("diamond")) return 40;
            return 25;
        }
        if ("mace".equals(pref) && name.contains("mace")) return 50;
        return 0;
    }

    private void select(int slot) {
        if (slot < 0 || slot > 8 || mc.player == null) return;
        if (realPackets.get()) {
            try { RealPackets.selectSlot(slot); }
            catch (Throwable t) {
                try {
                    var n = mc.getNetworkHandler();
                    if (n != null) n.sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slot));
                    mc.player.getInventory().selectedSlot = slot;
                } catch (Throwable ignored) {}
            }
        } else mc.player.getInventory().selectedSlot = slot;
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }

    public static AttributeSwap get() {
        try {
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("AttributeSwap");
            return m instanceof AttributeSwap a ? a : null;
        } catch (Throwable t) { return null; }
    }
}
