package ichttt.mods.firstaid.common.compat.playerrevive;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.common.util.CommonUtils;
import ichttt.mods.firstaid.common.util.HealthSync;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import team.creative.playerrevive.api.event.PlayerBleedOutEvent;
import team.creative.playerrevive.api.event.PlayerRevivedEvent;

/**
 * Keeps the First Aid damage model in sync with PlayerRevive's downed/revived state.
 */
public class PlayerReviveEventHandler {

    @SubscribeEvent
    public static void onPlayerRevived(PlayerRevivedEvent event) {
        Player player = event.getEntity();
        HealthSync.setBeingRevived(false);
        AbstractPlayerDamageModel damageModel = CommonUtils.getDamageModel(player);
        if (damageModel != null) {
            damageModel.revivePlayer(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerBleedOut(PlayerBleedOutEvent event) {
        HealthSync.setBeingRevived(false);
    }
}
