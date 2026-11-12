package org.bitwisemadness.journeyman.api;

import java.util.Set;
import java.util.UUID;

/**
 * The read side of the Journeyman engine, published as the {@code journeyman_api}
 * artifact for other mods (the quest layer above all) to compile against.
 *
 * <p>Pure Java - no Minecraft types - so the api jar carries no platform
 * weight. At runtime the engine mod embeds these classes and provides the
 * implementation through {@link java.util.ServiceLoader}; {@link #get()}
 * resolves it.</p>
 *
 * <p>Profession ids are namespaced strings ({@code "journeyman:miner"}).
 * Every read is safe for unknown ids: they read as absent rather than
 * throwing, so a typo is a quiet zero - the engine side is where unknown ids
 * are rejected loudly.</p>
 */
public interface JourneymanApi {

    /**
     * @return every loaded profession id, enabled or not
     */
    Set<String> professions();

    /**
     * @param professionId the profession id
     * @return whether that profession's definition is enabled; false when unknown
     */
    boolean isEnabled(String professionId);

    /**
     * @param playerId the player
     * @param professionId the profession id
     * @return the player's level there - at least 1 whenever the profession is
     *         known and the player is online (level 1 = untrained), 0 when the
     *         profession is unknown or the player is offline
     */
    int level(UUID playerId, String professionId);

    /**
     * @param playerId the player
     * @param professionId the profession id
     * @return the player's held XP there - 0 when unknown or offline
     */
    long xp(UUID playerId, String professionId);

    /**
     * @param playerId the player
     * @param professionId the profession id
     * @return whether the player has ever earned XP in that profession
     */
    boolean participated(UUID playerId, String professionId);

    /**
     * @return the resolved implementation; the engine mod registers it, so this
     *         throws only when Journeyman itself is absent
     */
    static JourneymanApi get() {
        return Holder.INSTANCE;
    }

    /** Lazy service handle; resolution happens on first use, never at class load. */
    final class Holder {

        private static final JourneymanApi INSTANCE = java.util.ServiceLoader.load(JourneymanApi.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "no JourneymanApi implementation on the classpath: the journeyman mod must be present"));

        private Holder() {
        }
    }
}
