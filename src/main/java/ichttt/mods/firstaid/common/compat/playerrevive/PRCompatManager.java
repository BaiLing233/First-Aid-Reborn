package ichttt.mods.firstaid.common.compat.playerrevive;

import ichttt.mods.firstaid.FirstAid;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Loads the real PlayerRevive bridge when PlayerRevive is installed.
 *
 * <p>PlayerRevive is a soft dependency. The bridge classes are only compiled when the PlayerRevive
 * jar is present in {@code libs/} (see build.gradle), so they are looked up reflectively: a build
 * without the jar simply keeps the no-op handler.</p>
 */
public class PRCompatManager {
    private static final String HANDLER_CLASS = "ichttt.mods.firstaid.common.compat.playerrevive.PRPresentCompatHandler";
    private static final String EVENT_CLASS = "ichttt.mods.firstaid.common.compat.playerrevive.PlayerReviveEventHandler";

    private static IPRCompatHandler handler = new NoopPRCompatHandler();

    public static IPRCompatHandler getHandler() {
        return handler;
    }

    public static void init() {
        if (!ModList.get().isLoaded("playerrevive")) {
            return;
        }
        try {
            Class<?> presentClass = Class.forName(HANDLER_CLASS);
            boolean canUse = (boolean) presentClass.getMethod("canUse").invoke(null);
            if (!canUse) {
                FirstAid.LOGGER.warn("PlayerRevive installed, but its bleeding attachment is not available - keeping no-op compatibility");
                return;
            }
            handler = (IPRCompatHandler) presentClass.getConstructor().newInstance();
            NeoForge.EVENT_BUS.register(Class.forName(EVENT_CLASS));
            FirstAid.LOGGER.info("PlayerRevive compatibility enabled");
        } catch (ClassNotFoundException e) {
            FirstAid.LOGGER.info("PlayerRevive installed, but this build was compiled without the compatibility bridge");
        } catch (Throwable throwable) {
            FirstAid.LOGGER.warn("PlayerRevive installed but its API could not be hooked, falling back to no-op compatibility", throwable);
        }
    }
}
