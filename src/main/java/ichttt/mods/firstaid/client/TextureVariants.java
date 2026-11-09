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
