package org.bitwisemadness.journeyman.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * One player's profession progression: held XP per profession and the
 * participation flag. Server-authoritative, persisted with the player and
 * copied on death - progression survives dying.
 *
 * <p>XP keys are namespaced profession id strings; unknown keys here are
 * inert data the engine never reads (the engine rejects unknown ids at the
 * door, so a stale key after a datapack change is harmless).</p>
 */
public final class PlayerProfessions {

    /** Persistence codec; {@code participated} is optional so old saves load. */
    public static final Codec<PlayerProfessions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.unboundedMap(Codec.STRING, Codec.LONG).fieldOf("xp").forGetter(state -> state.xp),
        Codec.STRING.listOf().xmap(Set::copyOf, List::copyOf)
            .optionalFieldOf("participated", Set.of()).forGetter(state -> state.participated)
    ).apply(instance, PlayerProfessions::new));

    private final Map<String, Long> xp;
    private final Set<String> participated;

    /** Fresh state: untrained in everything. */
    public PlayerProfessions() {
        this(new HashMap<>(), new HashSet<>());
    }

    private PlayerProfessions(Map<String, Long> xp, Set<String> participated) {
        this.xp = xp;
        this.participated = participated;
    }

    /**
     * @param professionId the namespaced profession id
     * @return the player's held XP there, zero when never granted
     */
    public long xp(String professionId) {
        return xp.getOrDefault(professionId, 0L);
    }

    /**
     * @param professionId the namespaced profession id
     * @return whether XP was ever earned in that profession
     */
    public boolean participated(String professionId) {
        return participated.contains(professionId);
    }

    /**
     * Adds XP and marks participation. The caller owns clamping - the engine
     * applies the cap before reaching here.
     *
     * @param professionId the namespaced profession id
     * @param amount the XP to add, already clamped to the cap
     */
    public void add(String professionId, long amount) {
        xp.merge(professionId, amount, Long::sum);
        participated.add(professionId);
    }

    /**
     * Sets XP directly (admin paths); a positive value marks participation.
     *
     * @param professionId the namespaced profession id
     * @param amount the XP to set
     */
    public void set(String professionId, long amount) {
        if (amount <= 0L) {
            xp.remove(professionId);
            return;
        }
        xp.put(professionId, amount);
        participated.add(professionId);
    }
}
