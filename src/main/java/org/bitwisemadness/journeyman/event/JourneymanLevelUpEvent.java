package org.bitwisemadness.journeyman.event;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

/**
 * Fired on the NeoForge event bus when a grant crosses one or more level
 * thresholds - once per accepted grant that changes the level, carrying both
 * ends of the jump.
 */
public class JourneymanLevelUpEvent extends Event {

    private final Player player;
    private final String professionId;
    private final int oldLevel;
    private final int newLevel;

    /**
     * @param player the player the XP was attributed to
     * @param professionId the namespaced profession id
     * @param oldLevel the level before the grant
     * @param newLevel the level after it
     */
    public JourneymanLevelUpEvent(Player player, String professionId, int oldLevel, int newLevel) {
        this.player = player;
        this.professionId = professionId;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    /** @return the player the XP was attributed to */
    public Player getPlayer() {
        return player;
    }

    /** @return the namespaced profession id */
    public String getProfessionId() {
        return professionId;
    }

    /** @return the level before the grant */
    public int getOldLevel() {
        return oldLevel;
    }

    /** @return the level after the grant */
    public int getNewLevel() {
        return newLevel;
    }
}
