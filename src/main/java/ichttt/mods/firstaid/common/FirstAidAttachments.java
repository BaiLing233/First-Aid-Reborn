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

package ichttt.mods.firstaid.common;

import java.util.function.Supplier;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.damagesystem.PlayerDamageModel;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Player damage model storage, replacing the 1.20.1 capability. */
public final class FirstAidAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, FirstAid.MODID);

    public static final Supplier<AttachmentType<AbstractPlayerDamageModel>> DAMAGE_MODEL =
            ATTACHMENT_TYPES.register("damage_model",
                    () -> AttachmentType.<net.minecraft.nbt.CompoundTag, AbstractPlayerDamageModel>serializable(PlayerDamageModel::new).build());

    private FirstAidAttachments() {
    }
}
