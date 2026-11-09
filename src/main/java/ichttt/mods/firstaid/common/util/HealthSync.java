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

package ichttt.mods.firstaid.common.util;

/**
 * Replaces the per-player state that used to live on {@code SynchedEntityDataWrapper}.
 *
 * <p>The 1.20.1 mod swapped the player's {@code entityData} field for a subclass that intercepted
 * health and absorption writes. In 1.21.1 that field is {@code final}, so the interception now
 * happens in mixins; this class carries the two flags those mixins consult.</p>
 */
public final class HealthSync {
    private static boolean bypass = false;
    private static boolean beingRevived = false;

    /** While set, health writes go straight to vanilla - used when First Aid itself writes. */
    public static void setBypass(boolean value) {
        bypass = value;
    }

    public static boolean isBypassed() {
        return bypass;
    }

    /** While set, incoming health writes are ignored (the player is waiting for a revive). */
    public static void setBeingRevived(boolean value) {
        beingRevived = value;
    }

    public static boolean isBeingRevived() {
        return beingRevived;
    }

    private HealthSync() {
    }
}
