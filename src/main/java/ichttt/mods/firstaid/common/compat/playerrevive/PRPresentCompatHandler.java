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
