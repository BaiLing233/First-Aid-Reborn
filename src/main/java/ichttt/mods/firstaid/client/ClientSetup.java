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
