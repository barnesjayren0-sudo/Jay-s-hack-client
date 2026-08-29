package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Click while attack held — sword/axe only optional. */
public class AutoClicker extends Module {

    public final NumberSetting minCps = new NumberSetting("MinCPS", "Min clicks/sec", 8, 4, 16, 1);
    public final NumberSetting maxCps = new NumberSetting("MaxCPS", "Max clicks/sec", 11, 5, 20, 1);
    public final BoolSetting weaponsOnly = new BoolSetting("WeaponsOnly", "Sword/axe only", true);
    public final BoolSetting onEntity = new BoolSetting("OnEntity", "Only when aiming entity", true);

    private long lastClick;
    private int nextDelay = 100;

    public AutoClicker() {
        super("AutoClicker", "Hold attack to click", Category.COMBAT);
        addSetting(minCps);
        addSetting(maxCps);
        addSetting(weaponsOnly);
        addSetting(onEntity);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.options == null) return;
        if (!mc.options.attackKey.isPressed()) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (onEntity.get()) {
            if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        }
        if (ClientSettings.cooldownCheck && mc.player.getAttackCooldownProgress(0.5f) < 0.85f) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < nextDelay) return;

        if (mc.crosshairTarget instanceof EntityHitResult ehr) {
            mc.interactionManager.attackEntity(mc.player, ehr.getEntity());
        } else {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        mc.player.swingHand(Hand.MAIN_HAND);

        int min = Math.max(1, minCps.getInt());
        int max = Math.max(min, maxCps.getInt());
        int cps = min + (int) (Math.random() * (max - min + 1));
        nextDelay = Math.max(45, 1000 / cps);
        nextDelay = Humanizer.delay(nextDelay, 12, 45, 180);
        lastClick = now;
    }
}
