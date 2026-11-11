package org.bitwisemadness.journeyman;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Placeholder JUnit suite. The CI {@code junit} check asserts a non-zero test
 * count so it cannot pass vacuously, so the repo keeps at least this file (and
 * adds real unit tests as logic grows).
 */
class JourneymanTest {

    @Test
    void modIdIsALowercasePathSegment() {
        // MOD_ID is a compile-time constant, so referencing it does not trigger
        // class initialization - this test needs no Minecraft runtime.
        assertTrue(Journeyman.MOD_ID.matches("[a-z][a-z0-9_]{2,63}"),
                "mod id must be a lowercase resource location path segment");
    }
}
