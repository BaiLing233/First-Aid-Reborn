package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MessageApplyAbsorption implements CustomPacketPayload {
    public static final Type<MessageApplyAbsorption> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "apply_absorption"));
    public static final StreamCodec<FriendlyByteBuf, MessageApplyAbsorption> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageApplyAbsorption::new);

    private final float amount;

    public MessageApplyAbsorption(FriendlyByteBuf buffer) {
        amount = buffer.readFloat();
    }

    public MessageApplyAbsorption(float amount) {
        this.amount = amount;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeFloat(amount);
    }

    public float amount() {
        return amount;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
