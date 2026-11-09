package ichttt.mods.firstaid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.HealthSync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

/**
 * Intercepts health writes, replacing the 1.20.1 {@code SynchedEntityDataWrapper} hack.
 *
 * <p>The original swapped the (then non-final) {@code entityData} field for a subclass whose
 * {@code set} method rerouted incoming health changes into the per-limb damage model. That field
 * is {@code final} in 1.21.1, so the same rerouting happens here.</p>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "setHealth", at = @At("HEAD"), cancellable = true)
    private void firstaid$setHealth(float health, CallbackInfo ci) {
        if (HealthSync.isBypassed()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player) || player.level().isClientSide) {
            return;
        }
        if (!CommonUtils.hasDamageModel(player)) {
            return;
        }
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (damageModel == null) {
            return;
        }

        if (health > player.getMaxHealth()) {
            damageModel.forEach(part -> part.currentHealth = part.getMaxHealth());
            return;
        }
        if (HealthSync.isBeingRevived()) {
            ci.cancel();
            return;
        }
        if (FirstAidConfig.watchSetHealth && Float.isFinite(health) && health > 0
                && player instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
            float orig = player.getHealth();
            if (orig > 0 && Float.isFinite(orig)) {
                if (FirstAidConfig.SERVER.scaleMaxHealth.get()) {
                    orig = Math.min(orig, (float) player.getAttributeValue(Attributes.MAX_HEALTH));
                }
                float healed = health - orig;
                if (Math.abs(healed) > 0.001F) {
                    if (healed < 0) {
                        DamageDistribution.handleDamageTaken(RandomDamageDistributionAlgorithm.getDefault(),
                                damageModel, -healed, player, player.damageSources().magic(), true, true);
                    } else {
                        HealthDistribution.addRandomHealth(healed, player, true);
                    }
                }
            }
            ci.cancel();
        }
    }
}
