package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MessageAddHealth implements CustomPacketPayload {
    public static final Type<MessageAddHealth> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "add_health"));
    public static final StreamCodec<FriendlyByteBuf, MessageAddHealth> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageAddHealth::new);

    private final float[] table;

    public MessageAddHealth(FriendlyByteBuf buffer) {
        this.table = new float[8];
        for (int i = 0; i < 8; i++) {
            this.table[i] = buffer.readFloat();
        }
    }

    public MessageAddHealth(float[] table) {
        this.table = table;
    }

    public void encode(FriendlyByteBuf buf) {
        for (float f : table)
            buf.writeFloat(f);
    }

    public float[] table() {
        return table;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
