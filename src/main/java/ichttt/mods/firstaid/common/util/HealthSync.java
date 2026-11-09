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
