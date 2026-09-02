# Plan: Lightning Expansion v1 — local build in Rain Settlement

Revised 2026-09-01. Supersedes the fadehost-server version of this plan. Target environment is
now the **local Prism instance + this packwiz repo**; the server is out of scope for v1.

Decision unchanged: build as a tiny Fabric mod (path 1a), executed in Claude Code. Not started.

## 1. Success criteria

- 1a. `lightning-expansion-<ver>.jar` builds from `mod/` with `./gradlew build` on this Mac.
- 1b. In a local single-player world in the `rain-settlement` Prism instance: a Lightning
  spell book is craftable at the Spell Binding Table, holds 5 lightning spells, and each casts
  without a client or server-thread exception in `logs/latest.log`.
- 1c. Lightning staff, wand, and 4 robe pieces exist as items, render in inventory and on the
  player model, and grant `spell_power:lightning` attribute values.
- 1d. `runes:lightning_stone` is craftable (Runes ships the item but no recipe — see 4b).
- 1e. The jar is published through packwiz (`pack/mods/`) at pack version 1.6.0 and a fresh
  instance sync installs it with no manual download prompt.
- 1f. No regression: all 163 existing pack entries still load; `latest.log` shows no new
  mixin/registry conflicts.

## 2. Environment (Rain Settlement pack 1.6.0, mirrored from Open-Air Settlement 1.0.30)

Fabric loader 0.19.3, MC 1.21.1, JVM target 21. As of 2026-09-01 this pack is content-identical
to the server's — see [pack-mirror-ledger-2026-09-01.md](pack-mirror-ledger-2026-09-01.md).
The environment block in the pre-mirror revision of this plan was written against the server's
`mods-list.txt` and is now correct again.

| id | version | role |
|---|---|---|
| spell_engine | 1.10.3+1.21.1 | spell schema, binding table, projectiles |
| spell_power | 1.6.0+1.21.1 | school attributes, damage types |
| wizards (RPG Series) | 3.1.1+1.21.1 | reference implementation, art source |
| elemental_wizards_rpg | 3.1.0+1.21.1 | third-party class-addon template |
| more_rpg_library | 2.7.1+1.21.1 | shared RPG plumbing |
| runes | 1.3.2+1.21.1 | spell reagents |
| armory / arsenal (RPG Series) | 1.5.1 / 1.5.0 | equipment configs |
| azurelibarmor | 3.1.3 | armor rendering (used by the More RPG Classes addons) |
| armor_model_api | 1.0.0+1.21.1 | armor rendering (used by wizards 3.1.1) |
| bettercombat | 2.4.0+1.21.1 | melee/weapon integration |
| structure_pool_api | 1.2.1+1.21.1 | wizards dep (loot structures) |
| accessories / trinkets | 1.1.0-beta.53 / 3.10.0 | equipment slots |
| skill_tree | 1.6.0+1.21.1 | class trees |

- 2a. **Robes: two rendering paths are present.** Wizards 3.1.1 `fabric.mod.json` depends on
  `armor_model_api`; the More RPG Classes addons (Elemental Wizards, Berserker, Forcemaster) use
  `azurelibarmor`. Follow whichever the chosen template uses — do not mix.
- 2b. **Item stat configs**: read the real paths from the synced instance rather than assuming.
  Candidates present are `config/rpg_series/`, `config/wizards/`, and `config/armory_rpgs/`;
  which exist depends on what has run at least once.
- 2c. **Distribution is packwiz-over-git, not "datapack on server + client".** One jar carries
  data, assets, and code; every instance picks it up on next launch.

## 3. Assumptions ledger

| # | Assumption | Load-bearing? | Status / cheapest validation |
|---|---|---|---|
| 3a | spell_power 1.6.0 ships a `lightning` school | Yes | **RESOLVED — true.** `SpellSchools` registers `LIGHTNING`; lang has `attribute.name.spell_power.lightning`, an Energize enchantment, and lightning potions. No custom school registration needed. |
| 3b | A lightning reagent item exists | Yes | **RESOLVED — partly.** `runes:lightning_stone` exists with model + texture, but Runes ships **no recipe** for it (only arcane/fire/frost/healing). We must ship one. |
| 3c | Spell-book generation is tag-driven | Yes | **RESOLVED.** Path is `data/<ns>/tags/spell/spell_book/<name>.json`, a list of `{id, required:false}`. Not `<ns>:spell_books/<name>` as previously written. Sibling tags: `tags/spell/spell_scroll/<name>.json`, `tags/spell/weapon/<x>_staff.json`, `tags/item/{staves,wands,wizard_robes}.json`. |
| 3d | Spell JSON schema is copyable from wizards | Yes | **RESOLVED.** `data/wizards/spell/arcane_bolt.json` is a complete template (school, tier, group, cast/release, target, deliver, impacts, cost). |
| 3e | Spell book creation is enabled | No | **RESOLVED.** `config/spell_engine/server.json5` → `spell_book_creation_enabled: true`. |
| 3f | A JDK 21 is available to build | **Yes — currently FALSE** | `/usr/bin/javac` is the macOS stub; Prism only has JREs. Fix in Phase 0. |
| 3g | packwiz-installer will not delete a hand-dropped dev jar | Yes | **RESOLVED.** `minecraft/packwiz.json` `cachedFiles` lists only pack-managed files; untracked jars are left alone. Dev iteration in the live instance is safe. |
| 3h | packwiz indexes a raw jar committed under `pack/mods/` and serves it from the raw.githubusercontent base URL | Yes | **RESOLVED.** The 1.6.0 mirror added raw (non-metafile) `config/*.json5` and `datapacks/*/` entries to `index.toml`; the installer fetches them relative to the pack URL. A raw jar takes the same path. Still confirm end-to-end in Phase 1. |
| 3i | Item attributes can be driven by a config file the way wizards does it | No | Fallback is registering attribute modifiers in Java. Decide in Phase 3 after reading the real config path (2b). |

## 4. Judgment calls already made

- 4a. **Mod id / namespace: `lightning_expansion`.** Separate namespace, not an override of
  `wizards`, so a wizards update never clobbers our data.
- 4b. **Reagent: reuse `runes:lightning_stone`.** Do not register a new stone. Ship the missing
  recipe in our namespace, copying `data/runes/recipe/fire_rune_small_hand.json`
  (`crafting_shapeless`, `runes:rune_crafting/base/stone` + a reagent tag → 2 stones). Pick
  an existing vanilla lightning-flavored reagent for the second ingredient rather than defining
  a new `runes:rune_crafting/reagent/lightning_small` tag if Runes doesn't already declare one.
- 4c. **No custom spell school.** Use `spell_power:lightning` (3a). This deletes the riskiest
  Java work from the original plan.
- 4d. **Testing is single-player only for v1.** No dedicated server, no fadehost. Multiplayer
  validation is a v2 concern.
- 4e. **Art: programmatic recolor via Pillow in a scratch venv.** No ImageMagick on this Mac and
  no reason to install it; the operation is a hue map on small PNGs.
- 4f. **Elemental Wizards RPG 3.1.0 is the addon template.** It is in the pack as of 1.6.0, so
  read its item/armor registration and spell-book tag layout directly from
  `mods/elemental_wizards_rpg-fabric-3.1.0+1.21.1.jar`. Note it renders armor through
  `azurelibarmor`, not `armor_model_api` (2a).

## 5. Phases

### Phase 0: Toolchain
- **Work**: install a JDK 21 (`brew install --cask temurin@21`); confirm
  `javac -version` reports 21. Create `mod/` and a scratch venv with Pillow for art work.
  Add `mod/build/`, `mod/.gradle/`, and `jars/` to `.gitignore`.
- **Judgment calls**: Temurin over `openjdk@21` — Prism already ships Temurin JREs, so the
  vendor matches what the game runs on. No system Gradle; the Loom template's wrapper handles it.
- **Dependencies**: Homebrew (present).
- **Exit criteria**: `javac -version` → 21.x; `python3 -c "import PIL"` succeeds in the venv.

### Phase 1: Distribution spike (before any content)
- **Work**: scaffold a Fabric mod from the Loom template (JDK 21, deps from Modrinth maven:
  spell_engine, spell_power, wizards, runes, armor_model_api) that registers exactly one item.
  Build it. Copy the jar to `~/Library/Application Support/PrismLauncher/instances/rain-settlement/minecraft/mods/`,
  launch, confirm the item exists. Then commit the same jar to `pack/mods/`, run
  `packwiz refresh`, bump `pack.toml` to 1.6.0, push, and relaunch the instance to confirm the
  installer pulls it from GitHub raw.
- **Judgment calls**: validate the whole delivery pipeline on a stub, because a packwiz raw-file
  failure (3h) would invalidate every later phase and costs 20 minutes to find here vs. days later.
- **Dependencies**: Phase 0; GitHub push access to `benji-hix/rain-settlement`.
- **Exit criteria**: the stub item appears in-game after a clean instance sync with no manual
  download prompt, and `packwiz.json` lists `mods/<jar>` as a tracked file. If the raw-file path
  fails, fall back to a GitHub Release URL added via `packwiz url add` and re-test before Phase 2.

### Phase 2: Spells
- **Work**: unzip wizards/spell_engine/runes/elemental-wizards-rpg into `jars/`. Author 5 spell
  JSONs under `data/lightning_expansion/spell/`, using `arcane_bolt.json` as the schema template
  and `spell_power:lightning` as the school: bolt, chain, storm AoE, static shield/buff, shock
  nova. Vanilla `lightning_bolt` + `electric_spark` particles. Add
  `data/lightning_expansion/tags/spell/spell_book/lightning.json` and the matching
  `spell_scroll` and `weapon/lightning_staff` tags. Ship the `runes:lightning_stone` recipe (4b).
- **Judgment calls**: 5 spells with one shared `group` and `cost.cooldown.group: "weapon"`,
  mirroring the wizards arcane set, so cooldowns interoperate with existing classes.
- **Dependencies**: Phase 1 exit.
- **Exit criteria**: Spell Binding Table produces a Lightning spell book; all 5 spells bind and
  cast; `latest.log` clean across a full cast of each; lightning runes craft.

### Phase 3: Items
- **Work**: register staff, wand, and 4 robe pieces. Wire robes through armor_model_api. Grant
  `spell_power:lightning` via the same mechanism wizards uses — read the actual config path in
  the synced instance first (2b), and fall back to Java attribute modifiers if it isn't
  data-driven (3i). Add `tags/item/{staves,wands,wizard_robes}` entries so existing systems
  (skill_tree, loot, JEI) see them.
- **Judgment calls**: match wizards' arcane-tier numbers rather than inventing a power curve —
  v1 is a parallel school, not a stronger one.
- **Dependencies**: Phase 2 exit (spells must exist for the staff to bind them).
- **Exit criteria**: all 6 items obtainable via `/give`, robes render on the player, tooltip
  shows Lightning Spell Power, staff casts a bound lightning spell.

### Phase 4: Art and publish
- **Work**: hue-map wizards' arcane textures (item, robe layers, spell_book, projectile fx) to
  cyan/yellow with Pillow; review each output as an image; hand-tweak the ones that read badly.
  Final build, full playtest pass, bump `pack.toml`, `packwiz refresh`, commit, push.
- **Judgment calls**: recolor rather than draw from scratch — the pack's visual identity is
  wizards', and consistency matters more than originality here. No More Wizards assets (ARR).
- **Dependencies**: Phase 3 exit.
- **Exit criteria**: 1a–1f all true; instance syncs the published version cleanly from a cold start.

## 6. Failure modes and mitigations

| # | Failure | Early signal | Mitigation |
|---|---|---|---|
| 6a | packwiz won't serve a raw jar from the repo | Phase 1 relaunch doesn't install the stub | GitHub Release + `packwiz url add`; decided before content work exists |
| 6b | Instance sync wipes the dev jar mid-iteration | Item vanishes after a launch | Disproved by 3g, but if it happens: clone the instance and strip `PreLaunchCommand` from `instance.cfg` for a dev-only instance |
| 6c | Spell Engine 1.10 schema differs from the 1.10.3 jar's own data (e.g. datagen-only fields) | Spell fails to load, registry error at world join | Copy whole files from `data/wizards/spell/` and mutate one field at a time; never author from memory |
| 6d | armor_model_api robe rendering needs a model the recolor can't supply | Robes render invisible or as vanilla leather | Ship robes as flat-texture armor first, model layer second; item stats are the load-bearing half |
| 6e | Scope creep into a skill_tree class tree | "while we're here, a lightning tree…" | Explicitly out of scope for v1; log as v2 |

**Replan trigger**: if any phase needs more than 5 build-test rounds, stop and re-scope rather
than continuing to iterate.

## 7. Critical path

Phase 0 → Phase 1 (distribution spike) → Phase 2 (spells) → Phase 3 (items) → Phase 4 (art).

Only Phase 3 depends on Phase 2's output; art (Phase 4) is independent of everything but the
final build and can be pulled forward to run alongside Phase 2–3 if you want to compress.
Nothing waits on another person or an external approval.

## 8. First 3 actions

1. `brew install --cask temurin@21`, then `javac -version`.
2. Fetch reference jars into `jars/` — every URL is already in the pack metafiles:
   ```
   mkdir -p jars && for f in wizards spell-engine spell-power runes; do
     m=pack/mods/$f.pw.toml
     curl -sL -o "jars/$(grep -m1 '^filename' $m | cut -d'"' -f2)" "$(grep -m1 '^url' $m | cut -d'"' -f2)"
   done
   ```
   plus `elemental-wizards-rpg` and `more-rpg-library` the same way — both are pack entries now (4f).
3. Scaffold the Fabric mod in `mod/` with a single stub item and run the Phase 1 spike.
