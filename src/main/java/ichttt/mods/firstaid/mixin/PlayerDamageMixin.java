/*
 * FirstAid Reborn
 * Copyright (C) 2017-2024 ichttt (original First Aid)
 * Copyright (C) 2026 BaiLing233 (1.21.1 NeoForge port)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.EventHandler;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Hooks the two points the damage pipeline needs.
 *
 * <p>{@code hurt} is used for the special cases that must bypass vanilla entirely (infinite damage).
 * {@code actuallyHurt} only applies the health change, so cancelling it after First Aid distributed
 * the damage keeps the rest of {@code hurt} - knockback, the post-hit invulnerability window and the
 * hurt animation - intact.</p>
 *
 * <p>Some mod combinations still end up without knockback, so after {@code hurt} returns the mixin
 * checks whether a {@code LivingKnockBackEvent} was fired for the player. If nothing knocked the
 * player back, the impulse is applied directly (bypassing the event, which may itself be cancelled)
 * so hits always feel like hits.</p>
 */
@Mixin(Player.class)
public abstract class PlayerDamageMixin {
    private static final double FALLBACK_KNOCKBACK = 0.4D;

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void firstaid$hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        EventHandler.clearKnockback(player);
        if (debug()) {
            FirstAid.LOGGER.info("[FirstAid/KB] hurt start: src={} direct={} amount={} invuln={} delta={}",
                    source.type().msgId(),
                    source.getDirectEntity() == null ? "null" : source.getDirectEntity().getType(),
                    amount, player.invulnerableTime, player.getDeltaMovement());
        }
        Boolean result = EventHandler.preHandleCustomPlayerDamage(player, source, amount);
        if (result != null) {
            cir.setReturnValue(result);
        }
    }

    @Inject(method = "actuallyHurt", at = @At("HEAD"), cancellable = true)
    private void firstaid$actuallyHurt(DamageSource source, float amount, CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (EventHandler.handleCustomPlayerDamage(player, source, amount)) {
            ci.cancel();
            // Vanilla sets hurtMarked in LivingEntity.hurt *before* the knockback is applied, and
            // ServerEntity only sends the velocity packet when that flag is set. First Aid replaces
            // the health step, so the flag is set here as well - otherwise the server moves the
            // player but the client never receives the motion and the hit looks knockback-less.
            player.hurtMarked = true;
        }
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void firstaid$ensureKnockback(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        if (player.level().isClientSide) {
            return;
        }
        boolean applied = Boolean.TRUE.equals(cir.getReturnValue());
        boolean knocked = EventHandler.consumeKnockback(player);
        boolean fallback = false;
        if (applied && !knocked && !source.is(DamageTypeTags.NO_KNOCKBACK)
                && FirstAidConfig.SERVER.forceKnockback.get()) {
            Entity attacker = source.getDirectEntity() != null ? source.getDirectEntity() : source.getEntity();
            if (attacker != null && attacker != player) {
                applyDirectKnockback(player, attacker);
                fallback = true;
            }
        }
        if (debug()) {
            FirstAid.LOGGER.info("[FirstAid/KB] hurt end: applied={} vanillaKnockback={} fallback={} delta={} hurtMarked={}",
                    applied, knocked, fallback, player.getDeltaMovement(), player.hurtMarked);
        }
    }

    /**
     * Applies the vanilla knockback maths directly instead of calling {@code knockback()}, so a
     * cancelled {@code LivingKnockBackEvent} (some mods cancel it) cannot swallow the impulse.
     */
    private static void applyDirectKnockback(Player player, Entity attacker) {
        double dx = attacker.getX() - player.getX();
        double dz = attacker.getZ() - player.getZ();
        while (dx * dx + dz * dz < 1.0E-5F) {
            dx = (Math.random() - Math.random()) * 0.01D;
            dz = (Math.random() - Math.random()) * 0.01D;
        }
        double strength = FALLBACK_KNOCKBACK * (1.0D - player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        if (strength <= 0.0D) {
            return;
        }
        Vec3 direction = new Vec3(dx, 0.0D, dz).normalize().scale(strength);
        Vec3 motion = player.getDeltaMovement();
        player.setDeltaMovement(
                motion.x / 2.0D - direction.x,
                player.onGround() ? Math.min(0.4D, motion.y / 2.0D + strength) : motion.y,
                motion.z / 2.0D - direction.z);
        player.hasImpulse = true;
        player.hurtMarked = true;
    }

    private static boolean debug() {
        return FirstAidConfig.SERVER != null && FirstAidConfig.SERVER.debugKnockback.get();
    }
}
