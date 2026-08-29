package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Hit selection: prefer crit window when target is grounded,
 * prefer grounded hits when target is mid-air (anti-jump reset).
 */
public class ComboHit extends Module {

    public final BoolSetting preferCrit = new BoolSetting("PreferCrit", "Wait for crit vs grounded", true);
    public final BoolSetting punishJump = new BoolSetting("PunishJump", "Hit grounded when they jump", true);

    public ComboHit() {
        super("ComboHit", "Crit vs grounded / punish jump", Category.COMBAT);
        addSetting(preferCrit);
        addSetting(punishJump);
    }

    /**
     * Shared gate for TriggerBot / KillAura / AimAssist.
     * @return true if this tick is a good time to attack target
     */
    public static boolean shouldAttack(PlayerEntity self, PlayerEntity target) {
        if (self == null || target == null) return true;
        Module mod = com.jay.hackclient.JayHackClient.moduleManager != null
                ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("ComboHit")
                : null;
        if (mod == null || !mod.isEnabled() || !(mod instanceof ComboHit ch)) return true;

        boolean targetAir = !target.isOnGround() && target.fallDistance < 0.05f && target.getVelocity().y > 0.05;

        if (ch.punishJump.get() && targetAir) {
            // Hit while we are grounded for knockback
            return self.isOnGround();
        }

        if (ch.preferCrit.get() && target.isOnGround()) {
            // Prefer vanilla crit window
            if (CritAssist.canAttackNow(self)) return true;
            // Allow normal hits if not falling yet and on ground with full cooldown
            return self.isOnGround() && self.getAttackCooldownProgress(0.5f) >= 0.92f;
        }

        return true;
    }
}
