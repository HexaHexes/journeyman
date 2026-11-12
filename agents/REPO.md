# Repo rules — Journeyman

Repo-specific rules and notes. The org-wide `AGENTS.md` beside this file is
maintained in [`neoforge-mod-template`](https://github.com/HexaHexes/neoforge-mod-template)
and pulled in verbatim — **never hand-edit it.** Everything in this file is this
repo's own and is edited freely; keep it aligned with the org rules rather than
restating them.

**Repo:** `HexaHexes/journeyman` • **Mod ID:** `journeyman` • **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 • **Jar:** `build/libs/journeyman-<version>.jar`

The pack's professions engine. Eighteen datapack-tunable professions — ten
trades and eight masteries — levelling on the RuneScape XP curve to level 99,
all progressing concurrently with no job slots: a profession's level is its own
gate. Every activity resolves at one interception point — check the gate, apply
the bonus, grant the XP. Mechanics source of truth: `docs/SKILLS.md`.

**Dependencies**
- Required: Minecraft, NeoForge.
- Optional: none yet. Every integration the profession set names (Tensura
  breeding and mounts, Ice and Fire, Apotheosis, Iron's Spells, Iron's Gems and
  Jewelry, Aquaculture, the Delight family, Dynamic Trees) arrives with its
  wave as an optional compat module, availability-gated the way AGENTS
  requires. The mod never gains a hard dependency on any of them.

**Test:** `./gradlew runGameTestServer` — `JourneymanGameTests` and
`JourneymanEngineGameTests` (7 tests). `expectedTests = 7` in `build.gradle`
must match the `@GameTest` count.

**Artifacts:** two publications from one repo. `journeyman` is the mod jar and
**embeds the api classes**; `journeyman_api` is the pure-Java read side other
mods compile against (`compileOnly`), resolved at runtime through
`ServiceLoader` — the services file ships inside the mod jar.

**Gotchas**
- The RuneScape curve is the single pacing source, and it is spec, not tuning:
  `docs/SKILLS.md` fixes level 99 at 13,034,431 XP and the GameTest asserts it.
  The floor placement is part of the spec: each term `l + 300·2^(l/7)` is
  floored, the terms are **summed**, and the total is divided by four and
  floored - a per-term division gives 275 at level 4 where the canon says 276.
  Never hand-adjust curve constants in code.
- **No-slots doctrine:** all 18 professions progress concurrently. Never
  reintroduce a selection or slot model - gates, not slots, are the
  progression.
- Every balance number (thresholds, magnitudes, maps) lives in
  `data/journeyman/professions/*.json`, never in code. The datapack is the
  tuning surface; the game-testing round edits data, not Java.
- Unknown profession ids are rejected loudly (log + no-op), never silently
  accepted - the SJ lesson: a typo must never become a silently
  uncompletable quest.
- `builder` is data-disabled by default (`enabled = false` in its definition).
  Re-enabling it is a datapack change, not code.
- Wave doctrine: intake listeners arrive one wave at a time and **all** XP
  flows through `ProfessionEngine.grant(...)` - no side doors. Events are
  observability, not control: they are not cancellable.
- **The `api` source set stays pure Java** - no Minecraft types - so the
  `journeyman_api` jar carries no platform weight. It is listed in the
  `neoForge.mods` block alongside main and gametest: dev runs load source-set
  outputs from the mod path, and a bare classpath line does NOT put the api
  classes in front of the GameTest server (that cost a debugging round).
- The services file (`META-INF/services/...JourneymanApi`) lives in main and
  must name the impl FQCN - part of the rename checklist if the package ever
  moves.
- Client-array mixins load nowhere in CI (gametests run a dedicated server):
  never `@Shadow` inherited Minecraft members, and launch `runClient` once
  before a release - full rules in `docs/mixin-rules.md`.

## Naming - this repo

- Repo and folder: `journeyman`; `mod_id`: `journeyman`;
  Java base package `org.bitwisemadness.journeyman`, with the Maven `group` in
  `build.gradle` matching.

## CI & release model - this repo

- `ci.yml` runs the template's checks (junit, gametest, static - SpotBugs +
  Checkstyle + Spotless) plus the version doctrine.
- **PRs never change the `mod_version` line** (neoforge-mod-template#81): the
  `-alpha.N` slot is owned by CI/CD. `version-bump.yml` bumps it on every real
  push to `main` and dispatches `release.yml`. Identity fields in
  `gradle.properties` ride with work freely; only a version bump must stay
  isolated.
- `publish.yml` uploads the artifact to this repository's GitHub Packages
  registry for the released version. Modrinth/CurseForge publishing needs
  their project ids and tokens set as repo secrets before it can run - a
  maintainer step when Journeyman first ships.
