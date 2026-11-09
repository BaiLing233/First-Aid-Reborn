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

package ichttt.mods.firstaid.client;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.api.damagesystem.AbstractDamageablePart;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.FirstAidState;
import ichttt.mods.firstaid.common.network.MessageAddHealth;
import ichttt.mods.firstaid.common.network.MessageApplyAbsorption;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessagePlayHurtSound;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.network.MessageUpdatePart;
import ichttt.mods.firstaid.common.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client-bound packet handling.
 *
 * <p>Kept out of the payload classes on purpose: those classes are registered on both sides, so
 * they must not reference client-only classes or a dedicated server refuses to load them.</p>
 */
public final class ClientPayloadHandlers {

    public static void updatePart(MessageUpdatePart message, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(Minecraft.getInstance().player);
            if (damageModel == null) return;
            AbstractDamageablePart damageablePart = damageModel.getFromEnum(message.part());
            damageablePart.setMaxHealth(message.maxHealth());
            damageablePart.setAbsorption(message.absorption());
            damageablePart.currentHealth = message.currentHealth();
        });
    }

    public static void addHealth(MessageAddHealth message, IPayloadContext context) {
        context.enqueueWork(() -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return;
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
            if (damageModel == null) {
                FirstAid.LOGGER.debug("Failed to find damage model, what?");
                return;
            }
            for (int i = 0; i < message.table().length; i++) {
                EnumPlayerPart part = EnumPlayerPart.VALUES[i];
                damageModel.getFromEnum(part).heal(message.table()[i], player, false);
            }
        });
    }

    public static void applyAbsorption(MessageApplyAbsorption message, IPayloadContext context) {
        context.enqueueWork(() -> {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(Minecraft.getInstance().player);
            if (damageModel == null) return;
            damageModel.setAbsorption(message.amount());
        });
    }

    public static void configuration(MessageConfiguration message, IPayloadContext context) {
        FirstAid.LOGGER.info("Received remote damage model");
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(mc.player);
            if (damageModel == null) return;
            damageModel.deserializeNBT(mc.level.registryAccess(), message.playerDamageModel());
            if (damageModel.hasTutorial)
                FirstAidState.tutorialDone.add(mc.player.getName().getString());
            else
                mc.player.sendSystemMessage(Component.literal("[First Aid] " + I18n.get("firstaid.tutorial.hint",
                        ClientHooks.SHOW_WOUNDS.getTranslatedKeyMessage().getString())));
            HUDHandler.INSTANCE.ticker = 200;
            FirstAid.isSynced = true;
            FirstAid.LOGGER.debug("Sync complete");
        });
    }

    public static void playHurtSound(MessagePlayHurtSound message, IPayloadContext context) {
        context.enqueueWork(() -> DebuffTimedSound.playHurtSound(message.sound(), message.duration()));
    }

    public static void syncDamageModel(MessageSyncDamageModel message, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(mc.player);
            if (damageModel == null) return;
            if (message.scaleMaxHealth())
                damageModel.runScaleLogic(mc.player);
            damageModel.deserializeNBT(mc.level.registryAccess(), message.playerDamageModel());
        });
    }

    private ClientPayloadHandlers() {
    }
}
