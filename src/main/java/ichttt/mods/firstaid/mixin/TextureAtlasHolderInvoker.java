package ichttt.mods.firstaid.mixin;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.TextureAtlasHolder;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the protected {@code getSprite} so the effect icon can be swapped at runtime. */
@Mixin(TextureAtlasHolder.class)
public interface TextureAtlasHolderInvoker {
    @Invoker("getSprite")
    TextureAtlasSprite firstaid$getSprite(ResourceLocation sprite);
}
