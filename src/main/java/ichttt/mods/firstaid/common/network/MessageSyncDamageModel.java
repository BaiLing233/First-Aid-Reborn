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

package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MessageSyncDamageModel implements CustomPacketPayload {
    public static final Type<MessageSyncDamageModel> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "sync_damage_model"));
    public static final StreamCodec<FriendlyByteBuf, MessageSyncDamageModel> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageSyncDamageModel::new);

    private final CompoundTag playerDamageModel;
    private final boolean scaleMaxHealth;

    public MessageSyncDamageModel(FriendlyByteBuf buffer) {
        this.playerDamageModel = buffer.readNbt();
        this.scaleMaxHealth = buffer.readBoolean();
    }

    public MessageSyncDamageModel(AbstractPlayerDamageModel damageModel, boolean scaleMaxHealth, HolderLookup.Provider provider) {
        this.playerDamageModel = damageModel.serializeNBT(provider);
        this.scaleMaxHealth = scaleMaxHealth;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeNbt(this.playerDamageModel);
        buffer.writeBoolean(scaleMaxHealth);
    }

    public CompoundTag playerDamageModel() {
        return playerDamageModel;
    }

    public boolean scaleMaxHealth() {
        return scaleMaxHealth;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
