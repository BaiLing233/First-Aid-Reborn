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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.client.TextureVariants;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/** Swaps the morphine effect icon for the redrawn one when the NEW texture set is selected. */
@Mixin(MobEffectTextureManager.class)
public abstract class MobEffectTextureManagerMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void firstaid$textureVariant(Holder<MobEffect> effect, CallbackInfoReturnable<TextureAtlasSprite> cir) {
        if (!TextureVariants.useNew()) {
            return;
        }
        ResourceLocation key = effect.unwrapKey().map(k -> k.location()).orElse(null);
        if (key == null || !key.equals(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "morphine"))) {
            return;
        }
        TextureAtlasSprite sprite = ((TextureAtlasHolderInvoker) (Object) this)
                .firstaid$getSprite(TextureVariants.morphineEffectSprite());
        if (sprite != null) {
            cir.setReturnValue(sprite);
        }
    }
}
