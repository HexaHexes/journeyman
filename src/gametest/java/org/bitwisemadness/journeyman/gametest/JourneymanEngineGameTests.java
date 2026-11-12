package org.bitwisemadness.journeyman.gametest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.bitwisemadness.journeyman.Journeyman;
import org.bitwisemadness.journeyman.api.XpCurve;
import org.bitwisemadness.journeyman.data.ProfessionRegistry;
import org.bitwisemadness.journeyman.engine.ProfessionEngine;
import org.bitwisemadness.journeyman.event.JourneymanLevelUpEvent;
import org.bitwisemadness.journeyman.state.JourneymanAttachments;
import org.bitwisemadness.journeyman.state.PlayerProfessions;

/**
 * Wave 0 engine behaviour: the curve, the default profession set, the grant
 * funnel (cap discard and loud rejection included) and the observability
 * events.
 *
 * <p>Keep the {@code expectedTests} count in {@code build.gradle} in sync with
 * the number of {@code @GameTest} methods across both holders, or the harness
 * fails the run.</p>
 */
@GameTestHolder(Journeyman.MOD_ID)
@PrefixGameTestTemplate(false)
public final class JourneymanEngineGameTests {

    private static final List<String> SPEC_IDS = List.of(
        "smith", "enchanter", "alchemist", "fisherman", "miner", "lumberjack", "digger", "farmer", "hunter", "builder",
        "melee", "armor", "ranged", "cook", "rancher", "riding", "jewelrer", "arcanist");

    private static final List<Integer> LEVEL_UPS = new CopyOnWriteArrayList<>();

    static {
        NeoForge.EVENT_BUS.addListener((JourneymanLevelUpEvent event) -> LEVEL_UPS.add(event.getNewLevel()));
    }

    private JourneymanEngineGameTests() {
    }

    /** The RuneScape curve is spec: cap and early table spots must match exactly. */
    @GameTest(template = "empty")
    public static void curve_matches_the_spec(GameTestHelper helper) {
        helper.assertTrue(XpCurve.xpToReach(XpCurve.LEVEL_CAP) == 13_034_431L,
            "level 99 must cost exactly 13,034,431 XP");
        helper.assertTrue(XpCurve.xpToReach(2) == 83L, "level 2 must cost 83 XP");
        helper.assertTrue(XpCurve.xpToReach(3) == 174L, "level 3 must cost 174 XP");
        helper.assertTrue(XpCurve.xpToReach(4) == 276L, "level 4 must cost 276 XP");
        helper.assertTrue(XpCurve.xpToReach(1) == 0L, "level 1 costs nothing");
        helper.assertTrue(XpCurve.levelForXp(0L) == 1, "zero XP is level 1");
        helper.assertTrue(XpCurve.levelForXp(83L) == 2, "83 XP is level 2");
        helper.assertTrue(XpCurve.levelForXp(Long.MAX_VALUE) == XpCurve.LEVEL_CAP, "the cap clamps");
        helper.succeed();
    }

    /** The default datapack set must load complete, with builder dormant. */
    @GameTest(template = "empty")
    public static void the_default_set_loads(GameTestHelper helper) {
        helper.assertTrue(ProfessionRegistry.count() == SPEC_IDS.size(),
            "the spec's eighteen professions must load");
        for (String specId : SPEC_IDS) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Journeyman.MOD_ID, specId);
            helper.assertTrue(ProfessionRegistry.definition(id) != null, specId + " must load");
        }
        helper.assertTrue(!ProfessionRegistry.isEnabled(ResourceLocation.fromNamespaceAndPath(Journeyman.MOD_ID, "builder")),
            "builder ships disabled by default");
        helper.assertTrue(ProfessionRegistry.isEnabled(ResourceLocation.fromNamespaceAndPath(Journeyman.MOD_ID, "miner")),
            "the trade professions ship enabled");
        helper.succeed();
    }

    /** A valid grant credits XP, marks participation and crosses the threshold. */
    @GameTest(template = "empty")
    public static void xp_grants_level_up(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayerInLevel();
        ProfessionEngine.Grant grant = ProfessionEngine.grant(player, "journeyman:miner", 83.0D, "gametest");
        helper.assertTrue(grant.granted(), "a valid grant must be accepted");
        helper.assertTrue(grant.newLevel() == 2, "83 XP is level 2");
        PlayerProfessions state = player.getData(JourneymanAttachments.PROFESSIONS.get());
        helper.assertTrue(state.xp("journeyman:miner") == 83L, "the XP must be credited");
        helper.assertTrue(state.participated("journeyman:miner"), "the grant must mark participation");
        helper.succeed();
    }

    /** XP past the cap is discarded, never banked: the cap holds exactly. */
    @GameTest(template = "empty")
    public static void xp_discards_at_the_cap(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayerInLevel();
        ProfessionEngine.grant(player, "journeyman:miner", 13_034_431.0D, "gametest");
        ProfessionEngine.Grant over = ProfessionEngine.grant(player, "journeyman:miner", 5_000.0D, "gametest");
        helper.assertTrue(!over.granted(), "a grant at the cap must be discarded");
        PlayerProfessions state = player.getData(JourneymanAttachments.PROFESSIONS.get());
        helper.assertTrue(state.xp("journeyman:miner") == XpCurve.XP_AT_CAP,
            "the cap must hold exactly " + XpCurve.XP_AT_CAP + " XP");
        helper.assertTrue(XpCurve.levelForXp(state.xp("journeyman:miner")) == XpCurve.LEVEL_CAP,
            "the capped player sits at level 99");
        helper.succeed();
    }

    /** Unknown profession ids are rejected loudly and change nothing. */
    @GameTest(template = "empty")
    public static void unknown_professions_are_rejected(GameTestHelper helper) {
        Player player = helper.makeMockServerPlayerInLevel();
        ProfessionEngine.Grant grant = ProfessionEngine.grant(player, "journeyman:nope", 100.0D, "gametest");
        helper.assertTrue(!grant.granted(), "unknown professions must be rejected");
        PlayerProfessions state = player.getData(JourneymanAttachments.PROFESSIONS.get());
        helper.assertTrue(state.xp("journeyman:nope") == 0L, "a rejected grant must credit nothing");
        helper.assertTrue(!state.participated("journeyman:nope"), "a rejected grant must not mark participation");
        helper.succeed();
    }

    /** Crossing a threshold fires the level-up event. */
    @GameTest(template = "empty")
    public static void level_up_events_fire(GameTestHelper helper) {
        LEVEL_UPS.clear();
        Player player = helper.makeMockServerPlayerInLevel();
        ProfessionEngine.grant(player, "journeyman:miner", 83.0D, "gametest");
        helper.assertTrue(LEVEL_UPS.contains(2), "crossing level 2 must fire the level-up event");
        helper.succeed();
    }
}
