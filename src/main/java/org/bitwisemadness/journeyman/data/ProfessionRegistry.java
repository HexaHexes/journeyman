package org.bitwisemadness.journeyman.data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import net.minecraft.resources.ResourceLocation;

/**
 * The loaded profession set: one immutable snapshot, replaced atomically on
 * every datapack load ({@code /reload} included), so a reload that drops a
 * file also drops what it declared.
 *
 * <p>Ids are namespaced ({@code "journeyman:miner"}); any pack may add
 * professions or override the defaults by carrying
 * {@code data/<namespace>/professions/<id>.json}.</p>
 */
public final class ProfessionRegistry {

    private static volatile Map<ResourceLocation, ProfessionDefinition> professions = Map.of();

    private ProfessionRegistry() {
    }

    /**
     * Replaces the snapshot; called by the loader on every reload.
     *
     * @param loaded the parsed set, in sorted location order
     */
    public static void set(Map<ResourceLocation, ProfessionDefinition> loaded) {
        professions = Collections.unmodifiableMap(new TreeMap<>(loaded));
    }

    /**
     * @param id the profession id
     * @return the definition, or null when unknown - callers decide whether
     *         that is an error (the engine treats it as one)
     */
    public static ProfessionDefinition definition(ResourceLocation id) {
        return professions.get(id);
    }

    /**
     * @param id the profession id
     * @return whether a definition exists and is enabled
     */
    public static boolean isEnabled(ResourceLocation id) {
        ProfessionDefinition definition = professions.get(id);
        return definition != null && definition.enabled();
    }

    /**
     * @return every loaded profession id, as strings, sorted
     */
    public static Set<String> ids() {
        Set<String> ids = new java.util.TreeSet<>();
        professions.keySet().forEach(id -> ids.add(id.toString()));
        return Set.copyOf(ids);
    }

    /**
     * @return how many professions the current snapshot holds
     */
    public static int count() {
        return professions.size();
    }
}
