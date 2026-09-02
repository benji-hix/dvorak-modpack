# Pack mirror ledger — rain settlement ← Open-Air Settlement (2026-09-01)

Rain Settlement `pack/` was replaced wholesale with Open-Air Settlement's `packwiz/` contents at
OAS pack version **1.0.30**, published here as rain settlement **1.6.0**. Rain's `index.toml` now
hashes byte-identical to OAS's (`8de8288ceb94f566…`), so the two packs are content-identical;
only `pack.toml`'s `name`/`author`/`version` differ.

**Why**: Rain Settlement is the local development and testing instance. The Lightning Expansion
mod has to be built against exactly what the server runs, and the two packs had diverged by 51
added / 16 removed / 21 re-versioned entries.

Source: `/Users/benji/Code/open-air-settlement/packwiz` (github.com/Voraque/open-air-settlement).
Pre-mirror snapshot: git `23eccac` (`pack/` at 1.5.0). Revert with `git checkout 23eccac -- pack`.

## 1. Downgrades taken — upgrade candidates for Open-Air

Rain was ahead on these. Each is a candidate for raising on the server; none has been tested
there. Items 1a–1c are a locked set — Open-Air's `CLAUDE.md` documents the conflict chain
("Iris 1.8.8 needs Sodium 0.6.x"; "Supplementaries newer than 3.6.7 needs Sodium 0.8.x, which
conflicts with Iris 1.8.8"). They move together or not at all.

| # | Mod | Open-Air (now Rain) | Rain had | Notes |
|---|---|---|---|---|
| 1a | Sodium | 0.6.13 | 0.8.12 | locked set with 1b, 1c |
| 1b | Iris Shaders | 1.8.8 | 1.8.14-beta.1 | locked set |
| 1c | Supplementaries | 3.6.7 | 3.9.3 | locked set |
| 1d | Cupboard | 1.21-2.9 | 1.21.1-4.0 | major version behind |
| 1e | Shoulder Surfing Reloaded | 4.10.5 | 5.0.11 | major version behind |
| 1f | Interactive Foliage | 1.1.1 | 1.3.0 | client-side |
| 1g | Gravestones (pneumono) | 1.2.6 | 1.4.2 | |
| 1h | Easy Mob Spawn Control | 1.5.7 | 1.5.8 | patch only |

## 2. Upgrades gained by Rain

Open-Air was ahead here; Rain picked these up with no action needed.

| # | Mod | Rain had | now |
|---|---|---|---|
| 2a | Ranged Weapon API | 2.3.3 | 3.0.0 |
| 2b | owo-lib | 0.12.15.4 | 0.13.0-alpha.15 |
| 2c | Particle Rain | 3.0.5 | 4.0.0-beta.11 |
| 2d | ScalableLux | 0.1.0.1 | 0.3.0-alpha.0.7 |
| 2e | Critters and Companions | 2.3.4 | 2.7.0 |
| 2f | Tom's Simple Storage | 2.3.0 | 2.4.1 |
| 2g | JEI | 19.39.0.368 | 19.44.0.403 |
| 2h | Tectonic | 3.0.22 | 3.0.26 |
| 2i | Balm | 21.0.58 | 21.0.65 |
| 2j | Entity Culling | 1.10.2 | 1.10.5 |
| 2k | ImmediatelyFast | 1.6.10 | 1.6.12 |
| 2l | LambDynamicLights | 4.8.8 | 4.8.10 |
| 2m | Traveler's Backpack | 10.1.36 | 10.1.38 |
| 2n | Custom Time Cycle | 0.1.4 | 0.1.6 | ⚠️ see 4a |

## 3. Dropped from Rain

3.1 Genuinely removed — nothing in Open-Air replaces these:

- 3a. Vanilla Backport 1.1.7.10 (and its dependency Platform 1.3.3) — added in Rain 1.4.0
- 3b. Modern UI 3.13.0.1
- 3c. Zoomify 2.15.2
- 3d. Critical Strike 1.0.4
- 3e. Ecological 0.3.0
- 3f. Item Components 1.1
- 3g. Resource packs: Better Leaves 9.5, Better Grass 1.21, Default Dark Mode + Darkomizer

3.2 Replaced by an Open-Air equivalent, not lost:

- 3h. The Aether 1.5.11 → Open-Air's patched build `aether-1.21.1-1.5.11-fabric+openair.1.jar`
- 3i. ~~Nullscape (was a mod) → now a datapack entry~~ — wrong; Nullscape was not removed. See 4e.
- 3j. Yori30's Grappling Hooks → Grappling Hook Mod: Skybound 1.1
- 3k. Craftable Gunpowder 1.4 → Craftable Gunpowder[MOD] 2.0
- 3l. Seramicx's Smooth F5 1.3.1 → the author's other build, 1.0.0
- 3m. PneumonoCore — same jar, different project entry; no change

## 4. Watch items on first launch

- 4a. **Custom Time Cycle is back to 0.1.6.** Rain commit `c5da4a6` (1.2.2) pinned it to 0.1.4
  because "bundled perms-api broke 1.21.1". Open-Air runs 0.1.6, so either the break was
  server-side, environment-specific, or since fixed. If the client crashes on launch, this is the
  first suspect — and it means Open-Air has a latent problem worth reporting back.
- 4b. Three mods are custom repacks served from `github.com/Voraque/open-air-settlement` releases
  (Aether patch, Fowl Play repack, Green Cuts build). Rain's sync now depends on those release
  assets staying published.
- 4c. The pack gained `datapacks/` and `config/` folders. `pack.toml` now sets
  `[options] datapack-folder = "datapacks"`. Two of those datapacks are raw folders, and
  `config/{fowlplay,nutritionz}.json5` are raw files — the installer fetches them relative to the
  pack URL rather than from a CDN.
- 4e. **Nullscape is in the pack twice.** Open-Air ships it as both a mod
  (`mods/nullscape.pw.toml` → `Nullscape_1.21.x_v1.2.14.jar`) and a datapack
  (`datapacks/nullscape.pw.toml` → `Nullscape_1.21_v1.2.14.zip`). These are two distributions of
  the same End worldgen at the same version. This came from Open-Air, not from mirroring — worth
  checking there before it causes a biome-source conflict.
- 4d. The instance at `~/Library/Application Support/PrismLauncher/instances/rain-settlement`
  was two pack versions behind (106 jars, pre-1.3.0). Its first sync after this change is a large
  one.

## 5. Side effects on the Lightning Expansion plan

`docs/lightning-expansion-v1-plan-2026-09-01.md` was written against pre-mirror Rain. After the
mirror, `azurelibarmor`, `more_rpg_library`, `elemental_wizards_rpg`, `bettercombat`, `armory`,
and `arsenal` are all present, which retires corrections 2a–2c in that plan. Assumption 3h
(packwiz serves raw non-metafile entries from the repo) is also now proven by 4c.

## 6. Rain-only divergence: side flags (1.6.1, 2026-09-01)

The 1.6.0 client launch failed at mod resolution: Tectonic 3.0.26 and Terralith 2.6.2 hard-depend
on Lithostitched, which Open-Air marks `side = "server"`, so a client install never receives it.
This also affects every Open-Air client installed through packwiz — worth reporting upstream.

Rain 1.6.1 marks all nine server-side entries as `side = "both"`: cropxp, data-trades,
custom-time-cycle, gazebos, lithostitched, noisium, nullscape, worn-path, yungs-better-dungeons.
Reason beyond the launch fix: single-player runs an integrated server, so a local test instance
must carry the server-side mods to match what Open-Air actually runs. Content and versions are
unchanged; only `index.toml` hashes now differ from Open-Air 1.0.30. Watch item 4a (Custom Time
Cycle 0.1.6 on the client) becomes live with this change.
