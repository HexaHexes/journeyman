package org.bitwisemadness.journeyman;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Journeyman - the pack's professions engine.
 *
 * <p>Eighteen datapack-tunable professions - ten trades and eight masteries -
 * levelling on the RuneScape XP curve to level 99, all progressing
 * concurrently with no job slots: a profession's level is its own gate. Every
 * activity resolves at one interception point - check the gate, apply the
 * bonus, grant the XP - attributed to the player who performed it. Mechanics
 * source of truth: {@code docs/SKILLS.md}.</p>
 */
@Mod(Journeyman.MOD_ID)
public final class Journeyman {

    public static final String MOD_ID = "journeyman";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Journeyman(IEventBus modBus) {
        LOGGER.info("Journeyman initialized");
    }
}
