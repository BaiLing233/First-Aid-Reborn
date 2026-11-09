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
import ichttt.mods.firstaid.common.FirstAidState;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageClientRequest implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MessageClientRequest> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "client_request"));
    public static final StreamCodec<FriendlyByteBuf, MessageClientRequest> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageClientRequest::new);

    private final Type type;

    public MessageClientRequest(FriendlyByteBuf buffer) {
        this.type = Type.TYPES[buffer.readByte()];
    }

    public MessageClientRequest(Type type) {
        this.type = type;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(type.ordinal());
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public enum Type {
        TUTORIAL_COMPLETE, REQUEST_REFRESH;

        private static final Type[] TYPES = values();
    }

    public static void handle(MessageClientRequest message, IPayloadContext context) {
        ServerPlayer player = CommonUtils.checkServer(context);
        if (message.type == Type.TUTORIAL_COMPLETE) {
            FirstAidState.tutorialDone.add(player.getName().getString());
            context.enqueueWork(() -> {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
                if (damageModel == null) return;
                damageModel.hasTutorial = true;
            });
        } else if (message.type == Type.REQUEST_REFRESH) {
            context.enqueueWork(() -> {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
                if (damageModel == null) return;
                FirstAidNetwork.sendToPlayer(player,
                        new MessageSyncDamageModel(damageModel, true, player.registryAccess()));
            });
        }
    }
}
