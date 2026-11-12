package org.bitwisemadness.journeyman.api;

/**
 * The RuneScape XP curve - Journeyman's single pacing source, and spec, not
 * tuning: {@code XP(L) = floor( (sum over l in [1, L-1] of floor(l + 300 *
 * 2^(l/7))) / 4 )}, reaching {@value #LEVEL_CAP} at {@value #XP_AT_CAP} XP.
 * The division by four applies to the <em>sum</em> of the floored terms, not
 * to each term - the ordering the RuneScape Wiki's own implementation uses,
 * and the reason the table is the canonical one. The GameTest suite asserts
 * the cap and table spots; never adjust the constants here without changing
 * the spec beside them.
 */
public final class XpCurve {

    /** Lowest reachable level; a fresh player sits here with zero XP. */
    public static final int MIN_LEVEL = 1;
    /** Highest reachable level; further XP is discarded, never banked. */
    public static final int LEVEL_CAP = 99;
    /** Total XP required to reach {@link #LEVEL_CAP}. */
    public static final long XP_AT_CAP = 13_034_431L;

    private static final long[] XP_TO_REACH = build();

    private XpCurve() {
    }

    /**
     * @param level the level, clamped into {@code [MIN_LEVEL, LEVEL_CAP]}
     * @return the total XP required to reach that level (zero at the minimum)
     */
    public static long xpToReach(int level) {
        int clamped = Math.max(MIN_LEVEL, Math.min(LEVEL_CAP, level));
        return XP_TO_REACH[clamped];
    }

    /**
     * @param xp the held XP, clamped to {@code [0, XP_AT_CAP]}
     * @return the level that XP reaches
     */
    public static int levelForXp(long xp) {
        long clamped = Math.max(0L, Math.min(XP_AT_CAP, xp));
        int level = MIN_LEVEL;
        while (level < LEVEL_CAP && XP_TO_REACH[level + 1] <= clamped) {
            level++;
        }
        return level;
    }

    private static long[] build() {
        long[] xp = new long[LEVEL_CAP + 1];
        long sum = 0L;
        for (int level = MIN_LEVEL; level < LEVEL_CAP; level++) {
            sum += (long) Math.floor(level + 300.0D * Math.pow(2.0D, level / 7.0D));
            xp[level + 1] = sum / 4L;
        }
        return xp;
    }
}
