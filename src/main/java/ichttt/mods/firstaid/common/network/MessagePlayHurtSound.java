package ichttt.mods.firstaid.common.network;

import java.util.Objects;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class MessagePlayHurtSound implements CustomPacketPayload {
    public static final Type<MessagePlayHurtSound> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "play_hurt_sound"));
    public static final StreamCodec<FriendlyByteBuf, MessagePlayHurtSound> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessagePlayHurtSound::new);

    private final SoundEvent sound;
    private final int duration;

    public MessagePlayHurtSound(FriendlyByteBuf buffer) {
        this(BuiltInRegistries.SOUND_EVENT.get(buffer.readResourceLocation()), buffer.readInt());
    }

    public MessagePlayHurtSound(SoundEvent sound, int duration) {
        this.sound = sound;
        this.duration = duration;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeResourceLocation(Objects.requireNonNull(BuiltInRegistries.SOUND_EVENT.getKey(sound)));
        buf.writeInt(duration);
    }

    public SoundEvent sound() {
        return sound;
    }

    public int duration() {
        return duration;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
