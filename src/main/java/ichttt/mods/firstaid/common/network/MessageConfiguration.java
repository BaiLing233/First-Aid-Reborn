package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MessageConfiguration implements CustomPacketPayload {
    public static final Type<MessageConfiguration> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "configuration"));
    public static final StreamCodec<FriendlyByteBuf, MessageConfiguration> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageConfiguration::new);

    private final CompoundTag playerDamageModel;

    public MessageConfiguration(CompoundTag model) {
        this.playerDamageModel = model;
    }

    public MessageConfiguration(FriendlyByteBuf buffer) {
        this(buffer.readNbt());
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(playerDamageModel);
    }

    public CompoundTag playerDamageModel() {
        return playerDamageModel;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
