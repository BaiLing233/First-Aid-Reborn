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

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import net.minecraft.server.level.ServerPlayer;
import ichttt.mods.firstaid.common.util.HealthSync;
import net.minecraft.world.entity.player.Player;

/**
 * Routes absorption through the damage model, replacing the {@code SynchedEntityDataWrapper}
 * interception of {@code DATA_PLAYER_ABSORPTION_ID}.
 */
@Mixin(Player.class)
public abstract class PlayerMixin {
    @Inject(method = "getAbsorptionAmount", at = @At("HEAD"), cancellable = true)
    private void firstaid$getAbsorption(CallbackInfoReturnable<Float> cir) {
        Player self = (Player) (Object) this;
        if (!self.isAlive() || !CommonUtils.hasDamageModel(self)) {
            return;
        }
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(self);
        if (damageModel != null) {
            cir.setReturnValue(damageModel.getAbsorption());
        }
    }

    @Inject(method = "internalSetAbsorptionAmount", at = @At("HEAD"))
    private void firstaid$setAbsorption(float amount, CallbackInfo ci) {
        if (HealthSync.isBypassed()) {
            return;
        }
        Player self = (Player) (Object) this;
        if (!CommonUtils.hasDamageModel(self)) {
            return;
        }
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(self);
        if (damageModel != null) {
            damageModel.setAbsorption(amount);
            // Absorption is stored in the damage model, so the client has to be told about the new
            // value - otherwise it keeps rendering zero golden hearts.
            // Only when the player is actually connected: this also runs while the player data is
            // being read from disk, where the connection is still null (that NPE used to abort the
            // whole player load and disconnect with "Invalid player data").
            if (self instanceof ServerPlayer serverPlayer && serverPlayer.connection != null) {
                FirstAidNetwork.sendToPlayer(serverPlayer, new MessageApplyAbsorption(amount));
            }
        }
    }
}
