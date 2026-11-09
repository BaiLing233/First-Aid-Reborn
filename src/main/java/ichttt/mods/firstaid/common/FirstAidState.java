package ichttt.mods.firstaid.common;

import java.util.HashSet;
import java.util.Set;

/** Session-only state that used to hang off CapProvider. */
public final class FirstAidState {
    public static final Set<String> tutorialDone = new HashSet<>();

    private FirstAidState() {
    }
}
