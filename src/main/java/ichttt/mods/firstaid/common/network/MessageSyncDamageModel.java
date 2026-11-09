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
