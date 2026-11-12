# Journeyman — Skills & Professions (mechanics source of truth)

This file is the **source of truth for mechanics**. When any question arises
about a profession, a gate, a bonus, a threshold or a number, this document
answers it, and code and data are corrected to match it — never the reverse.
Balance values live in the datapack (`data/journeyman/professions/`), not here;
this file fixes the *shape* of the system and the numbers the engine itself
asserts.

---

## Core rules

- **18 professions** total: **10 trade professions** (the everyday set every
  player touches) + **8 mastery professions** (the advanced set that deepens
  specific systems).
- Every profession levels on the **RuneScape XP curve** to **level 99**:
  `XP(L) = floor( (Σ_{l=1}^{L-1} floor(l + 300·2^(l/7))) / 4 )` — level 99 =
  **13,034,431** XP. The engine asserts this value in its GameTest. Note the
  floor placement: each term `l + 300·2^(l/7)` is floored, the terms are
  summed, and the *total* is divided by four and floored - the RuneScape
  Wiki's own implementation, not a per-term division.
- **No job slots.** All 18 professions progress concurrently — the RuneScape
  model. A profession's level is its own gate: training it unlocks the next
  tier of what it controls.
- All thresholds, magnitudes, and maps are **datapack-tunable**; no balance
  numbers are hard-coded.
- Every activity resolves at **one interception point**: check the gate →
  apply the bonus → grant XP, attributed to the player who performed the
  interaction. All intake flows through that single funnel.
- Intake matches the pack's world-shaping mods, never vanilla assumptions:
  **Dynamic Trees replaces vanilla trees**, so wood intake and log-drop
  bonuses resolve against Dynamic Trees branch blocks and their tags/data;
  crop intake covers the Farmer's Delight family; gem and ore intake follows
  Apotheosis' systems.
- `builder` is **disabled by default** and has no mechanics (vanilla XP only
  if re-enabled — a datapack change, not code).
- **Gates never gate combat against mobs** — only equipment, crafting, and
  world/creature interactions.

---

## Trade professions

| Profession | Role |
| --- | --- |
| **smith** | Gates crafting of weapons/tools/armor (per item/tier). Bonuses: armor quality (+armor/toughness) and weapon quality (+attack damage/speed) on craft, extra ingot at the smithing table, anvil repair cost reduction; Apotheosis salvaging bonuses + rarity gate; modded-forge gates (Ice And Fire dragonforge, LOTR Forge). XP: crafting gear, smithing, anvil repair, salvaging. |
| **enchanter** | Enchant quality (extra enchantments, higher applied levels, reduced XP + lapis), enchanted-gear anvil reduction + grindstone refunds; gates on max enchant level and cost scaling, Apotheosis affix rolls and reforging. XP: enchanting, infusion, reforging, affix-rolled crafts. |
| **alchemist** | Potion potency bonus — stronger (amplifier) and/or longer (duration) effects. XP: brewing. |
| **fisherman** | Fishing loot quality (treasure/junk odds, bonus catch chance, fish upgrades; Aquaculture loot tables included) + fishing-rod use gate. XP: catching fish. |
| **miner** | Pickaxe use gate; bonus drops from ore/stone. XP: mining blocks. |
| **lumberjack** | Axe use gate; bonus log drops from wood (Dynamic Trees-aware — see core rules). XP: chopping wood. |
| **digger** | Shovel use gate; bonus drops from earth/dirt/gravel. XP: digging blocks. |
| **farmer** | Hoe use gate; bonus crop yields on harvest. XP: harvesting crops. |
| **hunter** | Bonus mob drops on kill — the hunt loop: kill → XP → better drops. XP: killing mobs. |
| **builder** | Disabled by default; no mechanics. |

---

## Mastery professions

### melee / armor / ranged — the efficiency trio

Determine how much of an item's *potential* a player realizes in combat:
`effective stats = item potential (set by smith) × realization fraction (your
combat level)`. Tier-aware: tiers are computed at runtime from the installed
item pool (weighted-quantile clustering; configurable tier count and
distribution curve), and each tier binds to a required level parametrically.
**Any item with attack stats — including axes, pickaxes, shovels, and hoes —
is under melee proficiency when used to attack.** XP: melee hits/kills, combat
while armored/taking hits, ranged hits/kills — scaled by target difficulty.

### cook — all cooking and food quality

Deliberately owns cooking, moved off `farmer`. Gates vanilla
furnace/smoker/campfire cooking plus Farmer's Delight, Brewin' and Chewin',
and Dungeon's Delight cooking; grants the food-quality bonus (+hunger/
+saturation scaling). XP: all cooking activities.

### rancher — animal husbandry

Breeding gate (`mob → required level`), gathering-from-mobs gate
(shearing/milking), ranch drop bonuses (extra wool, milk/stew, eggs), and
bred-animal stat bonuses (+max health, +movement speed, horse genetics —
**per-generation override, never compounding; growth speed never modified**).
Ice And Fire egg hatching gate + hatch quality; Tensura breeding covers gating
the mod's existing meat-breeding (Blade Tiger explicit) plus
addon-implemented egg breeding for Armorsaurus, Evil Centipede, Black Spider,
Giant Ant, and Megalodon (feed-to-love → egg carrying both parents' data →
biome/dimension-restricted incubation → baby). XP: breeding, shearing,
milking, egg collection/hatching.

### riding — mounts

Mount gate (pig easy → horse/camel higher), mount stamina bonus (longer
sprint, faster jump-bar recharge), mounted defense bonus (rider damage
reduction while mounted). Extends to Ice And Fire mounts (dragons,
hippogryphs, cockatrices) and Tensura mounts (Direwolf, Evil Centipede, Black
Spider, Giant Ant, Megalodon, Armorsaurus, Blade Tiger — Monster Saddle
system; the gate is checked per mounting player). XP: mounting, distance
traveled, jumps/sprints.

### jewelrer — gems and jewelry

Bonus gem-ore drops; the Apotheosis gem loop — cutting/upgrading quality and
cost, socketing quality/cost, gem recovery on socket removal, gate on gem
rarity upgrades, gate on max socketable rarity — plus Iron's Gems and Jewelry
(`irons_jewelry`) crafting gate + jewelry stat bonus. Full loop: **mine →
cut/upgrade → socket/remove → rarity-upgrade → craft jewelry.** XP: gem
cutting/upgrading, socketing, removal, rarity upgrades, jewelry crafting, gem
mining.

### arcanist — magic

Iron's Spells gating: spell/school access by level, spellbook/scroll crafting
gate, mana efficiency bonus (cheaper casts), max mana bonus (larger pool,
additive and capped). Apotheosis kill-drop bonus: quality and yield of
affixes/sockets/gems from mobs the player kills — feeding the `jewelrer` and
`enchanter` inputs. XP: casting, learning spells, crafting/upgrading
spellbooks and scrolls, kills.

---

## Profession interplay

- `smith` potential × combat-tier realization (`melee`/`armor`/`ranged`)
  governs equipment power.
- `arcanist` kill drops feed `jewelrer` (gems) and `enchanter` (affixes)
  inputs.
- `rancher` bred stats and `riding` bonuses are entity/player-side, not
  item-side.
- **Gates never gate combat against mobs** — only equipment, crafting, and
  world/creature interactions.

---

## Engine model

- **Curve:** the RuneScape table above is computed, never stored by hand.
  XP above the cap is **discarded, never banked** (at level 99 the bar does
  not fill).
- **State:** per-player, per-profession XP and a participation flag, stored
  server-authoritatively. Participation means: earned XP in that profession at
  least once.
- **Data:** each profession is one datapack file under
  `data/journeyman/professions/` (id = filename): display, `enabled`, and —
  arriving with its wave — that profession's XP sources, gates, and bonus
  magnitudes. Unknown profession ids are rejected loudly, never silently
  accepted.
- **Level-up events** fire on every threshold crossing for other systems
  (quests, HUD) to observe.
