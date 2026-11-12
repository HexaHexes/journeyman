package org.bitwisemadness.journeyman.engine;

import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.bitwisemadness.journeyman.api.JourneymanApi;
import org.bitwisemadness.journeyman.api.XpCurve;
import org.bitwisemadness.journeyman.data.ProfessionRegistry;
import org.bitwisemadness.journeyman.state.JourneymanAttachments;
import org.bitwisemadness.journeyman.state.PlayerProfessions;

/**
 * The {@link JourneymanApi} implementation, resolved by consumers through
 * {@code ServiceLoader} (the services file ships in the mod jar).
 *
 * <p>Reads are online-only in wave 0: an offline player reads as absent
 * (level 0, no XP), which is honest - the quest layer above always observes
 * online players, and offline reads would need the save's player data, a
 * later concern.</p>
 */
public final class JourneymanApiImpl implements JourneymanApi {

    @Override
    public Set<String> professions() {
        return ProfessionRegistry.ids();
    }

    @Override
    public boolean isEnabled(String professionId) {
        ResourceLocation id = ResourceLocation.tryParse(professionId);
        return id != null && ProfessionRegistry.isEnabled(id);
    }

    @Override
    public int level(UUID playerId, String professionId) {
        PlayerProfessions state = stateFor(playerId);
        if (state == null || ResourceLocation.tryParse(professionId) == null) {
            return 0;
        }
        return XpCurve.levelForXp(state.xp(professionId));
    }

    @Override
    public long xp(UUID playerId, String professionId) {
        PlayerProfessions state = stateFor(playerId);
        return state == null ? 0L : state.xp(professionId);
    }

    @Override
    public boolean participated(UUID playerId, String professionId) {
        PlayerProfessions state = stateFor(playerId);
        return state != null && state.participated(professionId);
    }

    private PlayerProfessions stateFor(UUID playerId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return null;
        }
        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
        return player == null ? null : player.getData(JourneymanAttachments.PROFESSIONS.get());
    }
}
