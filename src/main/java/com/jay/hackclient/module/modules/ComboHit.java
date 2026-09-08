package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.CombatRequirements;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Hit selection — crit window vs grounded, punish jumpers.
 * Wired into TriggerBot / KillAura.
 */
public class ComboHit extends Module {

    public final BoolSetting preferCrit = new BoolSetting("PreferCrit", "Wait for crit vs grounded", true);
    public final BoolSetting punishJump = new BoolSetting("PunishJump", "Hit grounded when they jump", true);
    public final BoolSetting requireCooldown = new BoolSetting("Cooldown", "Need almost full cooldown", true);

    public ComboHit() {
        super("ComboHit", "Crit vs grounded / punish jump", Category.COMBAT);
        addSetting(preferCrit);
        addSetting(punishJump);
        addSetting(requireCooldown);
    }

    public static boolean shouldAttack(PlayerEntity self, PlayerEntity target) {
        if (self == null || target == null) return true;
        Module mod = com.jay.hackclient.JayHackClient.moduleManager != null
                ? com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("ComboHit")
                : null;
        if (mod == null || !mod.isEnabled() || !(mod instanceof ComboHit ch)) return true;

        if (ch.requireCooldown.get() && !CombatRequirements.cooldownReady(0.90f)) {
            return false;
        }

        boolean targetJumping = !target.isOnGround()
                && target.fallDistance < 0.05f
                && target.getVelocity().y > 0.05;

        if (ch.punishJump.get() && targetJumping) {
            return self.isOnGround();
        }

        if (ch.preferCrit.get() && target.isOnGround()) {
            if (Criticals.isActive() || CritAssist.canAttackNow(self)) {
                return CritAssist.canAttackNow(self) || self.getAttackCooldownProgress(0.5f) >= 0.95f;
            }
            // CritAssist off: still prefer full cooldown grounded hits
            return self.isOnGround() && self.getAttackCooldownProgress(0.5f) >= 0.92f;
        }

        return true;
    }
}
