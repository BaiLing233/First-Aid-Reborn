/*
 * FirstAid
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ichttt.mods.firstaid.common;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.common.network.FirstAidNetwork;
import ichttt.mods.firstaid.FirstAidConfig;
import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.distribution.IDamageDistributionAlgorithm;
import ichttt.mods.firstaid.api.enums.EnumPlayerPart;
import ichttt.mods.firstaid.common.damagesystem.distribution.DamageDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.HealthDistribution;
import ichttt.mods.firstaid.common.damagesystem.distribution.RandomDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.damagesystem.distribution.StandardDamageDistributionAlgorithm;
import ichttt.mods.firstaid.common.network.MessageConfiguration;
import ichttt.mods.firstaid.common.network.MessageSyncDamageModel;
import ichttt.mods.firstaid.common.registries.FirstAidRegistryLookups;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.compat.enhancedvisuals.EnhancedVisualsCompat;
import ichttt.mods.firstaid.common.util.CustomHealingItems;
import ichttt.mods.firstaid.common.util.PlayerSizeHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import ichttt.mods.firstaid.client.ClientHooks;
import net.neoforged.fml.loading.FMLEnvironment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.level.SleepFinishedTimeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.tuple.Pair;
import javax.annotation.Nullable;

import java.util.*;

public class EventHandler {
    public static final Random RAND = new Random();

    public static final Map<Player, Pair<Entity, HitResult>> hitList = new WeakHashMap<>();

    /**
     * Called from the {@code Player#hurt} mixin before vanilla processes the hit.
     *
     * @return {@code null} to let vanilla continue, otherwise the value that {@code hurt} should return.
     */
    @Nullable
    public static Boolean preHandleCustomPlayerDamage(Player player, DamageSource source, float amountToDamage) {
        if (player.level().isClientSide || !CommonUtils.hasDamageModel(player))
            return null;
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (damageModel == null) return null;

        if (amountToDamage == Float.MAX_VALUE || Float.isNaN(amountToDamage) || amountToDamage == Float.POSITIVE_INFINITY) {
            damageModel.forEach(damageablePart -> damageablePart.currentHealth = 0F);
            if (player instanceof ServerPlayer)
                FirstAidNetwork.sendToPlayer((ServerPlayer) player, new MessageSyncDamageModel(damageModel, false, player.registryAccess()));
            CommonUtils.killPlayer(damageModel, player, source);
            hitList.remove(player);
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Called from the {@code Player#actuallyHurt} mixin. Distributes the damage over the body parts
     * and cancels the vanilla health change - everything else in {@code hurt} (knockback, the
     * post-hit invulnerability window, the hurt animation) has already run by this point.
     */
    public static boolean handleCustomPlayerDamage(Player player, DamageSource source, float amountToDamage) {
        if (player.level().isClientSide || !CommonUtils.hasDamageModel(player))
            return false;
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (damageModel == null) return false;

        boolean addStat = amountToDamage < 3.4028235E37F;
        IDamageDistributionAlgorithm damageDistribution = FirstAidRegistryLookups.getDamageDistributions(source.type());

        if (source.is(DamageTypeTags.IS_PROJECTILE)) {
            Pair<Entity, HitResult> rayTraceResult = hitList.remove(player);
            if (rayTraceResult != null) {
                Entity entityProjectile = rayTraceResult.getLeft();
                EquipmentSlot slot = PlayerSizeHelper.getSlotTypeForProjectileHit(entityProjectile, player);
                if (slot != null) {
                    List<EnumPlayerPart> possibleParts = CommonUtils.getPartListForSlot(slot);
                    damageDistribution = new StandardDamageDistributionAlgorithm(Collections.singletonMap(slot, possibleParts), false, true);
                }
            }
        }
        if (damageDistribution == null) {
            // No given distribution found, and no projectile distribution either. Let's check if we can tell by the source where we should apply the damage, otherwise fall back to random
            damageDistribution = PlayerSizeHelper.getMeleeDistribution(player, source);
            if (damageDistribution == null) {
                damageDistribution = RandomDamageDistributionAlgorithm.getDefault();
            }
        }

        DamageDistribution.handleDamageTaken(damageDistribution, damageModel, amountToDamage, player, source, addStat, true);

        // EnhancedVisuals hooks exactly this point in vanilla (Player#actuallyHurt), which First Aid
        // cancels. Replay the call so its blood/screen effects keep working.
        EnhancedVisualsCompat.onPlayerDamaged(player, source, amountToDamage);

        hitList.remove(player);
        return true;
    }

    @SubscribeEvent
    public static void tickPlayers(PlayerTickEvent.Post event) {
        if (!event.getEntity().getAbilities().invulnerable) {
            if (!event.getEntity().isAlive()) return;
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(event.getEntity());
            if (damageModel == null) return;
            damageModel.tick(event.getEntity().level(), event.getEntity());
            hitList.remove(event.getEntity()); //Damage should be done in the same tick as the hit was noted, otherwise we got a false-positive
        }
    }

    /**
     * Handles items registered through the customHealingItems / customDebuffSuppressors config lists.
     * They are ordinary items from other mods, so the behaviour is injected here instead of through
     * an ItemHealing subclass.
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        CustomHealingItems.HealSpec heal = CustomHealingItems.getHeal(stack);
        if (heal != null) {
            // Do not run the vanilla use (eating etc.), just open the wound screen like bandages do.
            event.setCanceled(true);
            if (event.getLevel().isClientSide && FMLEnvironment.dist.isClient()) {
                ClientHooks.showGuiApplyHealth(event.getHand());
            }
            return;
        }

        CustomHealingItems.SuppressSpec suppressor = CustomHealingItems.getSuppressor(stack);
        if (suppressor != null) {
            event.setCanceled(true);
            if (!event.getLevel().isClientSide) {
                Player player = event.getEntity();
                if (CommonUtils.hasDamageModel(player)) {
                    player.addEffect(new MobEffectInstance(CommonUtils.holder(RegistryObjects.MORPHINE_EFFECT.get()),
                            suppressor.ticks(), 0, false, false));
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(1);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onSleepFinished(SleepFinishedTimeEvent event) {
        if (ModList.get().isLoaded("morpheus")) return;
        for (Player player : event.getLevel().players()) {
            if (player.isSleepingLongEnough()) {
                AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
                if (damageModel == null) return;
                damageModel.sleepHeal(player);
            }
        }
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        ResourceLocation tableName = event.getName();
        int bandage, plaster, morphine;
        NumberProvider bandageMax  = UniformGenerator.between(1, 3);
        NumberProvider plasterMax  = UniformGenerator.between(1, 5);
        NumberProvider morphineMax = UniformGenerator.between(1, 2);
        NumberProvider poolRolls   = ConstantValue.exactly(1.0F);
        if (tableName.equals(BuiltInLootTables.SPAWN_BONUS_CHEST)) {
            bandage = 8;
            plaster = 16;
            morphine = 4;
            morphineMax = ConstantValue.exactly(1);
        } else if (tableName.equals(BuiltInLootTables.STRONGHOLD_CORRIDOR) || tableName.equals(BuiltInLootTables.STRONGHOLD_CROSSING) || tableName.equals(BuiltInLootTables.ABANDONED_MINESHAFT)) {
            bandage = 20;
            plaster = 24;
            morphine = 8;
            poolRolls = UniformGenerator.between(0, 1);
        } else if (tableName.equals(BuiltInLootTables.VILLAGE_BUTCHER)) {
            bandage = 4;
            plaster = 20;
            morphine = 2;
            plasterMax = UniformGenerator.between(3, 8);
        } else if (tableName.equals(BuiltInLootTables.IGLOO_CHEST)) {
            bandage = 4;
            plaster = 8;
            morphine = 2;
            poolRolls = UniformGenerator.between(0, 1);
        } else if (tableName.equals(BuiltInLootTables.SHIPWRECK_SUPPLY)) {
            bandage = 4;
            plaster = 8;
            morphine = 2;
            bandageMax = UniformGenerator.between(1, 2);
            plasterMax = UniformGenerator.between(1, 3);
            morphineMax = ConstantValue.exactly(1);
            poolRolls = UniformGenerator.between(0, 1);
        } else {
            return;
        }
        LootPool.Builder builder = LootPool.lootPool().name("firstaid_main").setRolls(poolRolls);
        builder.add(LootItem.lootTableItem(RegistryObjects.BANDAGE::get)
                    .apply(SetItemCountFunction.setCount(bandageMax))
                    .setWeight(bandage)
                    .setQuality(0));
        builder.add(LootItem.lootTableItem(RegistryObjects.PLASTER::get)
                    .apply(SetItemCountFunction.setCount(plasterMax))
                    .setWeight(plaster)
                    .setQuality(0));
        builder.add(LootItem.lootTableItem(RegistryObjects.MORPHINE::get)
                    .apply(SetItemCountFunction.setCount(morphineMax))
                    .setWeight(morphine)
                    .setQuality(0));
        event.getTable().addPool(builder.build());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onHeal(LivingHealEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.isDeadOrDying() || !CommonUtils.hasDamageModel(entity))
            return;
        event.setCanceled(true);
        if (entity.level().isClientSide || !FirstAidConfig.SERVER.allowOtherHealingItems.get())
            return;
        float amount = event.getAmount();
        //Hacky shit to reduce vanilla regen
        if (Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(stackTraceElement -> stackTraceElement.getClassName().equals(FoodData.class.getName()))) {
            if (FirstAidConfig.SERVER.allowNaturalRegeneration.get())
                amount = amount * (float) (double) FirstAidConfig.SERVER.naturalRegenMultiplier.get();
        } else {
            amount = amount * (float) (double) FirstAidConfig.SERVER.otherRegenMultiplier.get();
        }
        if (FirstAidConfig.GENERAL.debug.get()) {
            CommonUtils.debugLogStacktrace("External healing: : " + amount);
        }
        HealthDistribution.distributeHealth(amount, (Player) entity, true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            FirstAid.LOGGER.debug("Sending damage model to {}", event.getEntity().getName());
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(event.getEntity());
            if (damageModel == null) return;
            if (damageModel.hasTutorial)
                FirstAidState.tutorialDone.add(event.getEntity().getName().getString());
            ServerPlayer playerMP = (ServerPlayer) event.getEntity();
            FirstAidNetwork.sendToPlayer(playerMP, new MessageConfiguration(damageModel.serializeNBT(playerMP.registryAccess())));
        }
    }

    @SubscribeEvent(priority =  EventPriority.LOW)
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        hitList.remove(event.getEntity());
    }

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        LevelAccessor world = event.getLevel();
        if (!world.isClientSide() && world instanceof Level)
            ((Level) world).getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(FirstAidConfig.SERVER.allowNaturalRegeneration.get(), ((Level) world).getServer());
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer) {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
            if (damageModel == null) return;
            FirstAidNetwork.sendToPlayer((ServerPlayer) player, new MessageSyncDamageModel(damageModel, true, player.registryAccess()));
        }
    }

        /** Players for which a LivingKnockBackEvent has already been fired during the current hurt call. */
    private static final java.util.Set<java.util.UUID> KNOCKBACK_SEEN = java.util.concurrent.ConcurrentHashMap.newKeySet();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onKnockback(net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        // A cancelled or zero-strength event means the player was NOT knocked back, so the fallback
        // in PlayerDamageMixin has to take over.
        if (event.isCanceled() || event.getStrength() <= 0.0F) {
            return;
        }
        KNOCKBACK_SEEN.add(player.getUUID());
    }

    /** @return true when vanilla fired a knockback event for this player since the last {@link #clearKnockback}. */
    public static boolean consumeKnockback(Player player) {
        return KNOCKBACK_SEEN.remove(player.getUUID());
    }

    public static void clearKnockback(Player player) {
        KNOCKBACK_SEEN.remove(player.getUUID());
    }

@SubscribeEvent
    public static void tagsUpdated(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            FirstAidRegistryLookups.init(event.getRegistryAccess(), event.getUpdateCause() == TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED);
        }
    }

    @SubscribeEvent
    public static void onServerStop(ServerStoppedEvent event) {
        FirstAid.LOGGER.debug("Cleaning up");
        FirstAidState.tutorialDone.clear();
        EventHandler.hitList.clear();
        FirstAidRegistryLookups.reset();
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        DebugDamageCommand.register(event.getDispatcher());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (!event.isEndConquered() && !player.level().isClientSide && player instanceof ServerPlayer) {
            AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
            if (damageModel == null) return;
            damageModel.runScaleLogic(player);
            damageModel.forEach(damageablePart -> damageablePart.heal(damageablePart.getMaxHealth(), player, false));
            damageModel.scheduleResync();
        }
    }
}
