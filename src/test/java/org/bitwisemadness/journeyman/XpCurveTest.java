package org.bitwisemadness.journeyman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.bitwisemadness.journeyman.api.XpCurve;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the RuneScape curve: the spec's cap, the early table spots,
 * the level lookup and the clamps. The GameTest suite repeats the cap
 * assertion in-engine; this suite keeps it cheap in the plain junit gate.
 */
class XpCurveTest {

    @Test
    void capMatchesTheSpec() {
        assertEquals(13_034_431L, XpCurve.xpToReach(99), "level 99 = 13,034,431 XP (docs/SKILLS.md)");
        assertEquals(13_034_431L, XpCurve.XP_AT_CAP, "the published constant must match the curve");
    }

    @Test
    void earlyTableSpotsMatchRuneScape() {
        assertEquals(0L, XpCurve.xpToReach(1), "level 1 costs nothing");
        assertEquals(83L, XpCurve.xpToReach(2), "level 2 = 83 XP");
        assertEquals(174L, XpCurve.xpToReach(3), "level 3 = 174 XP");
        assertEquals(276L, XpCurve.xpToReach(4), "level 4 = 276 XP");
        assertEquals(388L, XpCurve.xpToReach(5), "level 5 = 388 XP");
        assertEquals(512L, XpCurve.xpToReach(6), "level 6 = 512 XP");
        assertEquals(1154L, XpCurve.xpToReach(10), "level 10 = 1,154 XP");
        assertEquals(101_333L, XpCurve.xpToReach(50), "level 50 = 101,333 XP");
    }

    @Test
    void levelLookupIsTheCurveInverse() {
        assertEquals(1, XpCurve.levelForXp(0L), "zero XP is level 1");
        assertEquals(1, XpCurve.levelForXp(82L), "just under level 2 is still level 1");
        assertEquals(2, XpCurve.levelForXp(83L), "exactly level 2's cost reaches level 2");
        assertEquals(99, XpCurve.levelForXp(13_034_431L), "the cap's cost reaches the cap");
        assertEquals(99, XpCurve.levelForXp(Long.MAX_VALUE), "overflowing XP clamps to the cap");
    }

    @Test
    void lookupsClampInsteadOfThrowing() {
        assertEquals(0L, XpCurve.xpToReach(-3), "negative levels clamp to level 1");
        assertEquals(13_034_431L, XpCurve.xpToReach(500), "levels past the cap clamp to the cap");
        assertEquals(1, XpCurve.levelForXp(-1L), "negative XP clamps to zero");
    }

    @Test
    void theCurveIsStrictlyMonotonic() {
        long previous = 0L;
        for (int level = 2; level <= XpCurve.LEVEL_CAP; level++) {
            long current = XpCurve.xpToReach(level);
            assertTrue(current > previous, "the curve must strictly increase (broke at level " + level + ")");
            previous = current;
        }
    }
}
