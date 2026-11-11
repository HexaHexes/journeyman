# Journeyman

The pack's professions engine for Minecraft 1.21.1 (NeoForge).

Eighteen datapack-tunable professions — ten trades and eight masteries —
levelling on the RuneScape XP curve to level 99, all progressing concurrently
with **no job slots**: a profession's level is its own gate, and training it
unlocks the next tier of what it controls. Every activity resolves at one
interception point — check the gate, apply the bonus, grant the XP — attributed
to the player who performed the interaction. Gates never gate combat against
mobs; they gate equipment, crafting, and world/creature interactions.

**The profession set, its mechanics, and every number's home are specified in
[`docs/SKILLS.md`](docs/SKILLS.md)** — that file is the source of truth for
mechanics; this README only covers the mod itself.

## What the mod is

| | |
|---|---|
| Mod id | `journeyman` |
| Required dependencies | Minecraft, NeoForge — nothing else |
| Integrations | Tensura, Ice and Fire, Apotheosis, Iron's Spells, Iron's Gems and Jewelry, Aquaculture, the Delight family, Dynamic Trees — all **optional compat**, each availability-gated; the mod runs standalone without any of them |
| Progression model | All 18 professions level concurrently; gates, not slots |
| Tuning | Every threshold, magnitude and map lives in `data/journeyman/professions/*.json` |

## Verification

```
./gradlew build spotlessCheck
./gradlew runGameTestServer
```

The GameTest harness grades the run on its log summary; `expectedTests` in
`build.gradle` must match the `@GameTest` count. CI runs junit, gametest,
static (SpotBugs + Checkstyle + Spotless) and the version doctrine: the
`-alpha.N` version slot is owned by CI/CD, never by a pull request.

## License

GNU LGPL v2.1 — see `LICENSE`. Third-party systems read through public APIs
are attributed in `NOTICE`.
