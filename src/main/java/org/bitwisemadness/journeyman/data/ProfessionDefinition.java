package org.bitwisemadness.journeyman.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * One profession's datapack definition, loaded from
 * {@code data/<namespace>/professions/<id>.json} (id = filename).
 *
 * <p>Wave 0 ships the set itself: the eighteen ids, whether each is enabled,
 * and its display key. XP sources, gates and bonus magnitudes extend this
 * schema with their wave - the schema is additive by design.</p>
 *
 * @param enabled whether the profession participates in the game; the
 *                spec's {@code builder} ships {@code false}
 * @param display the translation key for the profession's display name
 */
public record ProfessionDefinition(boolean enabled, String display) {

    /** Datapack codec; {@code enabled} defaults true so a minimal file is valid. */
    public static final Codec<ProfessionDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.BOOL.optionalFieldOf("enabled", true).forGetter(ProfessionDefinition::enabled),
        Codec.STRING.optionalFieldOf("display", "").forGetter(ProfessionDefinition::display)
    ).apply(instance, ProfessionDefinition::new));
}
