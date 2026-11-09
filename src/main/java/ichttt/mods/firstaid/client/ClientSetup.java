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

package ichttt.mods.firstaid.client;

import org.spongepowered.asm.mixin.Unique;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.RegistryObjects;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/** Registers the item model predicate used by the texture set switch. */
@EventBusSubscriber(modid = FirstAid.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ClientSetup {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            var selector = (net.minecraft.client.renderer.item.ClampedItemPropertyFunction)
                    (stack, level, entity, seed) -> TextureVariants.modelSelector();
            ItemProperties.register(RegistryObjects.BANDAGE.get(), TextureVariants.ITEM_TEXTURE_MODE, selector);
            ItemProperties.register(RegistryObjects.PLASTER.get(), TextureVariants.ITEM_TEXTURE_MODE, selector);
            ItemProperties.register(RegistryObjects.MORPHINE.get(), TextureVariants.ITEM_TEXTURE_MODE, selector);
        });
    }

    private ClientSetup() {
    }
}
