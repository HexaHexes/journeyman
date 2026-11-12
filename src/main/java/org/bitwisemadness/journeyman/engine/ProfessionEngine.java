package org.bitwisemadness.journeyman.engine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.bitwisemadness.journeyman.api.XpCurve;
import org.bitwisemadness.journeyman.data.ProfessionDefinition;
import org.bitwisemadness.journeyman.data.ProfessionRegistry;
import org.bitwisemadness.journeyman.event.JourneymanLevelUpEvent;
import org.bitwisemadness.journeyman.event.JourneymanXpGainedEvent;
import org.bitwisemadness.journeyman.state.JourneymanAttachments;
import org.bitwisemadness.journeyman.state.PlayerProfessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The engine's single door. Every XP grant in every wave flows through
 * {@link #grant(Player, String, double, String)} - one interception point:
 * resolve and check the profession, apply the cap, credit the player, fire
 * the observability events.
 *
 * <p>Unknown profession ids are rejected loudly (a typo must never become
 * silently lost progression); a disabled profession ({@code builder}) is
 * rejected quietly, because its dormancy is intentional; XP at or past the
 * cap is discarded, never banked.</p>
 */
public final class ProfessionEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfessionEngine.class);

    /**
     * The outcome of one grant attempt.
     *
     * @param granted whether XP was credited
     * @param applied the XP actually applied (less than requested only at the cap)
     * @param oldLevel the level before the grant
     * @param newLevel the level after it
     */
    public record Grant(boolean granted, long applied, int oldLevel, int newLevel) {

        /** Shared rejection outcome; carries no data. */
        public static final Grant REJECTED = new Grant(false, 0L, 0, 0);
    }

    private ProfessionEngine() {
    }

    /**
     * The one interception point: check the gate, apply the bonus, grant the
     * XP. Wave 0 carries no gates or bonuses (their schema arrives with their
     * waves); the funnel is where they plug in, which is why every caller
     * comes here.
     *
     * @param player the acting player, who receives the credit
     * @param professionId the namespaced profession id
     * @param amount the requested XP
     * @param source a short intake label for the events
     * @return the grant outcome
     */
    public static Grant grant(Player player, String professionId, double amount, String source) {
        ResourceLocation id = ResourceLocation.tryParse(professionId);
        if (id == null) {
            LOGGER.warn("Rejected profession id '{}': not a valid resource location", professionId);
            return Grant.REJECTED;
        }
        ProfessionDefinition definition = ProfessionRegistry.definition(id);
        if (definition == null) {
            LOGGER.warn("Rejected XP grant for unknown profession '{}' (source: {})", professionId, source);
            return Grant.REJECTED;
        }
        if (!definition.enabled()) {
            LOGGER.debug("Profession '{}' is disabled; discarded {} XP from {}", professionId, amount, source);
            return Grant.REJECTED;
        }
        if (amount <= 0.0D) {
            LOGGER.warn("Rejected non-positive XP grant ({} for '{}' from {})", amount, professionId, source);
            return Grant.REJECTED;
        }

        PlayerProfessions state = player.getData(JourneymanAttachments.PROFESSIONS);
        String key = id.toString();
        long before = state.xp(key);
        if (before >= XpCurve.XP_AT_CAP) {
            LOGGER.debug("'{}' is at the cap ({}); discarded {} XP from {}", key, XpCurve.XP_AT_CAP, amount, source);
            return Grant.REJECTED;
        }
        long applied = Math.min(XpCurve.XP_AT_CAP, before + Math.round(amount)) - before;
        state.add(key, applied);

        int oldLevel = XpCurve.levelForXp(before);
        int newLevel = XpCurve.levelForXp(before + applied);
        NeoForge.EVENT_BUS.post(new JourneymanXpGainedEvent(player, professionId, applied, source));
        if (newLevel > oldLevel) {
            NeoForge.EVENT_BUS.post(new JourneymanLevelUpEvent(player, professionId, oldLevel, newLevel));
        }
        return new Grant(true, applied, oldLevel, newLevel);
    }
}
