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
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

/**
 * Natural regeneration heals the hidden vanilla health, which First Aid then routes into the body
 * parts through the {@code setHealth} hook. This gate lets a pack disable it entirely with
 * {@code naturalRegenMultiplier = 0}.
 */
@Mixin(FoodData.class)
public abstract class FoodDataMixin {
    @ModifyVariable(method = "tick", at = @At("STORE"), ordinal = 0)
    private boolean firstaid$gateNaturalRegen(boolean naturalRegen, Player player) {
        if (!naturalRegen || !CommonUtils.hasDamageModel(player)) {
            return naturalRegen;
        }
        return FirstAidConfig.SERVER.naturalRegenMultiplier.get() > 0.0D;
    }
}
