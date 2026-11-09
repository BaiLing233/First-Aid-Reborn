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
