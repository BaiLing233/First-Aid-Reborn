package ichttt.mods.firstaid.common.compat.playerrevive;

import ichttt.mods.firstaid.FirstAid;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import team.creative.playerrevive.PlayerRevive;
import team.creative.playerrevive.api.IBleeding;

/**
 * PlayerRevive 1.21.1 bridge. The bleeding state is an attachment
 * ({@link PlayerRevive#BLEEDING}), not a capability.
 */
public class PRPresentCompatHandler implements IPRCompatHandler {

    static {
        FirstAid.LOGGER.info("Initializing PlayerRevive Compatibility...");
    }

    public static boolean canUse() {
        return PlayerRevive.BLEEDING != null;
    }

    private static IBleeding getBleedingIfPossible(Player player) {
        if (player == null || player.getServer() == null) {
            return null;
        }
        if (!player.hasData(PlayerRevive.BLEEDING)) {
            return null;
        }
        return player.getData(PlayerRevive.BLEEDING);
    }

    @Override
    public boolean tryKnockOutPlayer(Player player, DamageSource source) {
        IBleeding bleeding = getBleedingIfPossible(player);
        if (bleeding != null && !bleeding.isBleeding()) {
            bleeding.knockOut(player, source);
            return true;
        }
        return false;
    }

    @Override
    public boolean isBleeding(Player player) {
        IBleeding bleeding = getBleedingIfPossible(player);
        return bleeding != null && bleeding.isBleeding() && bleeding.timeLeft() > 0;
    }
}
