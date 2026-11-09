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
import ichttt.mods.firstaid.client.ClientPayloadHandlers;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

/**
 * 1.21.1 networking entry point, replacing the old SimpleChannel.
 *
 * <p>Client-bound handlers live in {@code ClientPayloadHandlers}. They are only instantiated on a
 * physical client: a dedicated server must not load client-only classes.</p>
 */
public final class FirstAidNetwork {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("4");
        boolean client = FMLEnvironment.dist.isClient();

        registrar.playToClient(MessageUpdatePart.TYPE, MessageUpdatePart.STREAM_CODEC,
                client ? (payload, context) -> ClientPayloadHandlers.updatePart(payload, context) : noop());
        registrar.playToClient(MessageConfiguration.TYPE, MessageConfiguration.STREAM_CODEC,
                client ? (payload, context) -> ClientPayloadHandlers.configuration(payload, context) : noop());
        registrar.playToClient(MessageApplyAbsorption.TYPE, MessageApplyAbsorption.STREAM_CODEC,
                client ? (payload, context) -> ClientPayloadHandlers.applyAbsorption(payload, context) : noop());
        registrar.playToClient(MessageAddHealth.TYPE, MessageAddHealth.STREAM_CODEC,
                client ? (payload, context) -> ClientPayloadHandlers.addHealth(payload, context) : noop());
        registrar.playToClient(MessagePlayHurtSound.TYPE, MessagePlayHurtSound.STREAM_CODEC,
                client ? (payload, context) -> ClientPayloadHandlers.playHurtSound(payload, context) : noop());
        registrar.playToClient(MessageSyncDamageModel.TYPE, MessageSyncDamageModel.STREAM_CODEC,
                client ? (payload, context) -> ClientPayloadHandlers.syncDamageModel(payload, context) : noop());

        registrar.playToServer(MessageApplyHealingItem.TYPE, MessageApplyHealingItem.STREAM_CODEC,
                (payload, context) -> MessageApplyHealingItem.handle(payload, context));
        registrar.playToServer(MessageClientRequest.TYPE, MessageClientRequest.STREAM_CODEC,
                (payload, context) -> MessageClientRequest.handle(payload, context));

        FirstAid.LOGGER.debug("Registered 8 network payloads");
    }

    private static <T extends CustomPacketPayload> IPayloadHandler<T> noop() {
        return (payload, context) -> {
        };
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    public static void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    private FirstAidNetwork() {
    }
}
