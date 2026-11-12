package org.bitwisemadness.journeyman.state;

import java.util.function.Supplier;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The player attachment carrying {@link PlayerProfessions}.
 *
 * <p>Registered into NeoForge's attachment-type registry, persisted through
 * the state's codec (written with the player's data, read back on load), and
 * copied on death - progression survives dying.</p>
 */
public final class JourneymanAttachments {

    /** Registry hook; registered against the mod bus in the constructor. */
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
        DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, "journeyman");

    /** The per-player profession progression attachment. */
    public static final Supplier<AttachmentType<PlayerProfessions>> PROFESSIONS =
        ATTACHMENT_TYPES.register("professions",
            () -> AttachmentType.builder(PlayerProfessions::new)
                .serialize(PlayerProfessions.CODEC)
                .copyOnDeath()
                .build());

    private JourneymanAttachments() {
    }
}
