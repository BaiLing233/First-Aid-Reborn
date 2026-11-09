package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class MessageUpdatePart implements CustomPacketPayload {
    public static final Type<MessageUpdatePart> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "update_part"));
    public static final StreamCodec<FriendlyByteBuf, MessageUpdatePart> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageUpdatePart::new);

    private final byte id;
    private final int maxHealth;
    private final float absorption;
    private final float currentHealth;

    public MessageUpdatePart(FriendlyByteBuf buf) {
        this.id = buf.readByte();
        this.maxHealth = buf.readInt();
        this.absorption = buf.readFloat();
        this.currentHealth = buf.readFloat();
        validate();
    }

    public MessageUpdatePart(AbstractDamageablePart part) {
        this.id = (byte) part.part.ordinal();
        this.maxHealth = part.getMaxHealth();
        this.absorption = part.getAbsorption();
        this.currentHealth = part.currentHealth;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(id);
        buf.writeInt(maxHealth);
        buf.writeFloat(absorption);
        buf.writeFloat(currentHealth);
        validate();
    }

    private void validate() {
        if (currentHealth < 0) throw new RuntimeException("Negative currentHealth!");
        if (absorption < 0) throw new RuntimeException("Negative absorption!");
        if (maxHealth < 0) throw new RuntimeException("Negative maxHealth!");
        if (EnumPlayerPart.VALUES[id].ordinal() != this.id) throw new RuntimeException("Wrong player mapping!");
    }

    public EnumPlayerPart part() {
        return EnumPlayerPart.VALUES[id];
    }

    public int maxHealth() {
        return maxHealth;
    }

    public float absorption() {
        return absorption;
    }

    public float currentHealth() {
        return currentHealth;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
