package ichttt.mods.firstaid.common.compat.enhancedvisuals;

import java.lang.reflect.Method;

import ichttt.mods.firstaid.FirstAid;
import ichttt.mods.firstaid.FirstAidConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

/**
 * EnhancedVisuals hooks {@code Player#actuallyHurt} with its own mixin to spawn blood and screen
 * effects. First Aid cancels {@code actuallyHurt} (the per-limb model applies the damage instead),
 * so that injection never runs and EnhancedVisuals looks "installed but dead".
 *
 * <p>This bridge replays the same call EnhancedVisuals would have made, using reflection so that
 * EnhancedVisuals stays a soft dependency.</p>
 */
public final class EnhancedVisualsCompat {
    private static Object events;
    private static Method damageMethod;

    private EnhancedVisualsCompat() {
    }

    public static void init() {
        if (!ModList.get().isLoaded("enhancedvisuals")) {
            return;
        }
        // Never read the config here: this runs during setup, before ModConfigSpec values are
        // available ("Cannot get config value before config is loaded"). The toggle is checked
        // lazily in onPlayerDamaged instead.
        try {
            Class<?> mainClass = Class.forName("team.creative.enhancedvisuals.EnhancedVisuals");
            events = mainClass.getField("EVENTS").get(null);
            damageMethod = events.getClass().getMethod("damage", Player.class, DamageSource.class, float.class);
            FirstAid.LOGGER.info("EnhancedVisuals detected, compatibility bridge ready");
        } catch (Throwable throwable) {
            events = null;
            damageMethod = null;
            FirstAid.LOGGER.warn("EnhancedVisuals installed but its damage hook could not be reached, visual effects may stay inactive", throwable);
        }
    }

    /**
     * Lets EnhancedVisuals run its damage visuals for a hit First Aid has already distributed.
     */
    public static void onPlayerDamaged(Player player, DamageSource source, float amount) {
        if (damageMethod == null || amount <= 0F) {
            return;
        }
        if (!isEnabled()) {
            return;
        }
        try {
            damageMethod.invoke(events, player, source, amount);
        } catch (Throwable ignored) {
            // never let a cosmetic integration break the damage pipeline
        }
    }

    private static boolean isEnabled() {
        try {
            return FirstAidConfig.SERVER.enhancedVisualsCompat.get();
        } catch (IllegalStateException configNotLoadedYet) {
            return false;
        }
    }
}
