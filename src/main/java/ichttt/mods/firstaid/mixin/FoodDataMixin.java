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
