package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.resources.ResourceLocation;

/** Resolves texture paths for the configurable NEW / OLD texture sets. */
public final class TextureVariants {
    public static final ResourceLocation ITEM_TEXTURE_MODE = ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "texture_mode");

    public static boolean useNew() {
        return FirstAidConfig.CLIENT.textureMode.get() == FirstAidConfig.Client.TextureMode.NEW;
    }

    /** The value the item model override predicate compares against. */
    public static float modelSelector() {
        return useNew() ? 1.0F : 0.0F;
    }

    public static ResourceLocation healthTexture() {
        return gui(useNew() ? "simple_health_new" : "simple_health");
    }

    /**
     * Sprite name of the morphine effect icon inside the {@code mob_effects} atlas.
     *
     * <p>The vanilla atlas uses a {@code directory} source over {@code textures/mob_effect}, so every
     * file in that folder is registered with its bare file name - {@code firstaid:morphine_new}, not
     * {@code firstaid:mob_effect/morphine_new}.</p>
     */
    public static ResourceLocation morphineEffectSprite() {
        return ResourceLocation.fromNamespaceAndPath(FirstAid.MODID,
                useNew() ? "morphine_new" : "morphine");
    }

    private static ResourceLocation gui(String name) {
        return ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "textures/gui/" + name + ".png");
    }

    private TextureVariants() {
    }
}
