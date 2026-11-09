package ichttt.mods.firstaid.common.network;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPartHealer;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.api.healing.ItemHealing;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.CustomHealingItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class MessageApplyHealingItem implements CustomPacketPayload {
    public static final Type<MessageApplyHealingItem> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(FirstAid.MODID, "apply_healing_item"));
    public static final StreamCodec<FriendlyByteBuf, MessageApplyHealingItem> STREAM_CODEC =
            StreamCodec.of((buf, msg) -> msg.encode(buf), MessageApplyHealingItem::new);

    private final EnumPlayerPart part;
    private final InteractionHand hand;

    public MessageApplyHealingItem(FriendlyByteBuf buffer) {
        this.part = EnumPlayerPart.VALUES[buffer.readByte()];
        this.hand = buffer.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    public MessageApplyHealingItem(EnumPlayerPart part, InteractionHand hand) {
        this.part = part;
        this.hand = hand;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(part.ordinal());
        buf.writeBoolean(hand == InteractionHand.MAIN_HAND);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MessageApplyHealingItem message, IPayloadContext context) {
        ServerPlayer player = CommonUtils.checkServer(context);
        context.enqueueWork(() -> {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
            if (damageModel == null) return;
            ItemStack stack = player.getItemInHand(message.hand);
            // The healer keeps a reference to its stack for NBT saving, so hand it a snapshot taken
            // before the item is consumed. Otherwise the stored stack ends up empty and serialising
            // the damage model throws "Cannot encode empty ItemStack" on the next damage event.
            ItemStack healerStack = stack.copyWithCount(1);
            AbstractPartHealer healer = null;
            if (healerStack.getItem() instanceof ItemHealing itemHealing) {
                healer = itemHealing.createNewHealer(healerStack);
            } else {
                // Items added through the customHealingItems config behave like bandages/plaster.
                CustomHealingItems.HealSpec spec = CustomHealingItems.getHeal(healerStack);
                if (spec != null) {
                    healer = CustomHealingItems.createHealer(spec, healerStack);
                }
            }
            if (healer == null) {
                FirstAid.LOGGER.warn("Player {} has invalid item in hand {} while it should be an healing item",
                        player.getName(), BuiltInRegistries.ITEM.getKey(stack.getItem()));
                player.sendSystemMessage(Component.literal("Unable to apply healing item!"));
                return;
            }
            stack.shrink(1);
            AbstractDamageablePart damageablePart = damageModel.getFromEnum(message.part);
            damageablePart.activeHealer = healer;
        });
    }
}
