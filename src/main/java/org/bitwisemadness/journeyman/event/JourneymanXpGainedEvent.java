package org.bitwisemadness.journeyman.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge event bus whenever a player is granted XP in a
 * profession - once per accepted grant, with the amount actually applied
 * (cap-clamped grants apply less than requested).
 *
 * <p>Observability, not control: this event is not cancellable, because XP
 * intake has exactly one door and the checks happen before it.</p>
 */
public class JourneymanXpGainedEvent extends Event {

    private final Player player;
    private final String professionId;
    private final long applied;
    private final String source;

    /**
     * @param player the player the XP was attributed to
     * @param professionId the namespaced profession id
     * @param applied the XP actually applied after clamping
     * @param source a short intake label (e.g. {@code "gametest"}, a wave's
     *               activity id) for logging and future routing
     */
    public JourneymanXpGainedEvent(Player player, String professionId, long applied, String source) {
        this.player = player;
        this.professionId = professionId;
        this.applied = applied;
        this.source = source;
    }

    /** @return the player the XP was attributed to */
    public Player getPlayer() {
        return player;
    }

    /** @return the namespaced profession id */
    public String getProfessionId() {
        return professionId;
    }

    /** @return the XP actually applied after clamping */
    public long getApplied() {
        return applied;
    }

    /** @return the intake label the grant came through */
    public String getSource() {
        return source;
    }
}
