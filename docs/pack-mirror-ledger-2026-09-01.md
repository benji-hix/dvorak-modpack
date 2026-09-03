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

## 7. Custom Time Cycle pinned to 0.1.4 again (1.6.2, 2026-09-01)

Watch item 4a fired on the first world join: `NoSuchMethodError ServerPlayer.method_64396` from
`fabric-permissions-api` via Do a Barrel Roll's config handshake. Cause confirmed from the jars:
Custom Time Cycle 0.1.6 bundles fabric-permissions-api 0.3.3 (built for 1.21.2+), which outranks
the 0.2 that Do a Barrel Roll 3.7.3 bundles. Rain 1.6.2 restores the 0.1.4 pin from commit
`c5da4a6`. Open-Air runs 0.1.6 server-side next to Do a Barrel Roll; whether the dedicated server
avoids this through another permissions provider is unverified and worth checking there.

## 8. JVM segfaults on the bundled Microsoft JDK (2026-09-01)

Two HotSpot native crashes in the Rain instance, both on Prism's `java-runtime-delta`
(Microsoft OpenJDK 21.0.7+6, macOS 27, Apple M5):

- 2026-08-27 22:09 (`hs_err_pid97841.log`, instance then named dvorak, no lightning mod):
  `SIGSEGV` in `Chunk::chop()` on C2 CompilerThread0.
- 2026-09-01 22:06 (`hs_err_pid81241.log`): `SIGSEGV` in `SymbolTable::do_lookup` on the Render
  thread during `KnotClassLoader.loadClass` from `PlayerRenderer`, while five Distant Horizons
  render-loader threads were defining classes. The world had been created, the spell registry
  synced, and the player had joined; no frame in the crashed thread names lightning_expansion.

Open-Air's instance runs the same JDK with `JvmArgs=-XX:+UseZGC` and plays for hours; Rain ran
the default G1, which Distant Horizons warns about at startup. Decision: match Open-Air
(`-XX:+UseZGC`, Override Java args on). If it recurs, switch Rain's Java to the Temurin 21.0.12
JRE Prism already has (`java/eclipse_temurin_jre21.0.12+8`). Unverified either way.

Also observed, pre-existing and non-fatal: Just Enough Resources throws
`NoClassDefFoundError: jeresources.api.drop.LootDrop` in both JEI plugin passes; stale
`critical_strike:*` attribute references from the removed Critical Strike mod remain in this
instance's configs.

### 8a. ZGC did not fix it; switched to Temurin (2026-09-02)

Third native crash, `hs_err_pid90680.log`, 2026-09-02 00:56, after 2h25m of uptime and ~15s in
the Test world: `SIGSEGV` on ZGC worker thread `XWorker#1` in
`XLiveMap::iterate_segment(ObjectClosure*, unsigned long, unsigned long, unsigned long)`. The
crashed thread has no Java frames — this is inside the collector itself (`XLiveMap` is JDK 21's
non-generational ZGC). Command line confirms `-XX:+UseZGC` was active, so section 8's decision
was in force and did not hold.

Taking section 8's stated fallback: Rain's `JavaPath` now points at
`java/eclipse_temurin_jre21.0.12+8` (Temurin 21.0.12+8) instead of `java-runtime-delta`
(Microsoft 21.0.7+6); `JvmArgs=-XX:+UseZGC` kept, matching Open-Air. `JavaSignature` dropped so
Prism recomputes it; previous config saved as `instance.cfg.bak-2026-09-02`. Open-Air is still on
the Microsoft JDK and is untouched — if Rain proves stable on Temurin, that is the next thing to
mirror. Unverified until Rain plays a long session without a native crash.

Note on attribution: the macOS crash report for the 22:06 run (`pid 81241`, launched 22:04:34)
predates `glider-accessory-0.1.0.jar` being copied into `mods/` at 22:26, so neither that crash
nor this one implicates the new mod. `glider_accessory 0.1.0` loaded in the 00:56 session with no
mixin error, and its data pack was picked up on world load ("Found new data pack glider_accessory").

## 9. Parity raises applied to both packs (1.7.0, 2026-09-03)

Reviewed the live Rain↔Open-Air delta against Open-Air 1.0.31. Only 11 files differed, and
most of it was section 6's deliberate side-flag divergence. Two Open-Air bugs and eight
version raises came out of it. Revert tag in both repos: `pre-parity-2026-09-03`
(Rain `9aac0e8`, Open-Air `a707760`).

### 9.1 Open-Air bugs fixed upstream (OAS `66ffce3`, promoted 1.0.32)

- 9a. **Lithostitched was `side = "server"`** while Terralith 2.6.2 (`depends lithostitched
  >=1.7.7`) and Tectonic 3.0.26 (`depends lithostitched *`) were `side = "both"`. Every
  packwiz client install of Open-Air failed at Fabric mod resolution. This is section 6's
  finding, now fixed at the source instead of worked around in Rain. Open-Air had already hit
  this exact fault in `7794429` and fixed the two direct dependents without following the
  chain down. Only Lithostitched was flipped; the other eight server-side entries stay
  `server` upstream, because a dedicated server does not need to ship them to clients.
- 9b. **Nullscape shipped twice** (watch item 4e), same Modrinth project `LPjGiSO4` at 1.2.14,
  as both `mods/nullscape.pw.toml` (jar, server) and `datapacks/nullscape.pw.toml` (zip,
  both). Kept the mod. The datapack copy installed to `<root>/datapacks`, which is not a
  world datapack path on either side, so it was probably never loading. Removed in both packs.

### 9.2 Version raises — the section 1 downgrades, reversed

Rain took all eight on `main` (1.7.0) because Rain is the testbed. Open-Air took only the
client-only four on `main` (1.0.32); the four both-side raises are staged unmerged on branch
`server-window/both-side-raises` (1.0.33) and must land with a server update in the same pass.

| # | Mod | was | now | side | where |
|---|---|---|---|---|---|
| 1a | Sodium | 0.6.13 | 0.8.12 | client | both packs, main |
| 1b | Iris Shaders | 1.8.8 | 1.8.14-beta.1 | client | both packs, main |
| 1e | Shoulder Surfing Reloaded | 4.10.5 | 5.0.11 | client | both packs, main |
| 1f | Interactive Foliage | 1.1.1 | 1.3.0 | client | both packs, main |
| 1c | Supplementaries | 3.6.7 | 3.9.3 | both | Rain main; OAS branch |
| 1d | Cupboard | 1.21-2.9 | 1.21.1-4.0 | both | Rain main; OAS branch |
| 1g | Gravestones | 1.2.6 | 1.4.2 | both | Rain main; OAS branch |
| 1h | Easy Mob Spawn Control | 1.5.7 | 1.5.8 | both | Rain main; OAS branch |

Every jar filename above matches what Rain ran pre-mirror at `23eccac`, so these are versions
this machine has already booted at least once — except Supplementaries, where Rain ran 3.9.3
and Open-Air's own newest is 3.9.7 (not taken; 3.9.3 is the proven build).

### 9.3 The locked set, resolved

Section 1's "locked set" claim was right about the coupling and wrong about the direction.
Verified from Modrinth: Iris 1.8.14-beta.1 is the only 1.21.1 Iris above 1.8.8, and it names
Sodium `0.8.12-beta.1` as a required version. So Iris forces Sodium up, not down. Supplementaries
was never chained to Sodium at all — 3.9.7 declares exactly one dependency, Moonlight. Open-Air's
`CLAUDE.md` line "Supplementaries newer than 3.6.7 needs Sodium 0.8.x" describes an observed
runtime incompat, not metadata, and raising Sodium simply removes the blocker. Corrected in
Open-Air's `CLAUDE.md`.

Dependency floors read from the jars themselves, not the listings:

| jar | requires | pack pins | headroom |
|---|---|---|---|
| supplementaries 3.9.3 | `moonlight >=1.21-3.5.0` | Moonlight 3.5.0 | none, exactly at floor |
| easy_mob_spawn_control 1.5.8 | `fabricloader >=0.19.3` | Loader 0.19.3 | none, exactly at floor |
| gravestones 1.4.2 | `pneumonocore >=1.3.0` | PneumonoCore 1.3.1 | one patch |
| cupboard 1.21.1-4.0 | no new deps | — | Cave Spelunking 4.0 wants `cupboard "*"` |

### 9.4 Not taken

- 9c. **Custom Time Cycle stays split**: Rain 0.1.4 (section 7), Open-Air 0.1.6. Section 7 left
  this open. It is now closed: on Open-Air, Custom Time Cycle is `side = "server"` and Do a
  Barrel Roll is `side = "client"`, so the two never co-load and the bundled
  fabric-permissions-api 0.3.3 cannot be reached. Rain's integrated server is what put them in
  one JVM. No change needed upstream.
- 9d. Modern UI, Critical Strike, Ecological, Item Components, Vanilla Backport (section 3.1)
  stay dropped. These are content additions, not parity restoration.
- 9e. Zoomify and the four resource packs (Better Leaves, Better Grass, Default Dark Mode,
  Darkomizer) stay dropped. Client-only and free to re-add to either pack; a taste call, not a
  mirror fix.

### 9.5 Rain caught up on Gliding Accessories

Rain's pack had no `gliding-accessories` entry while Open-Air 1.0.31 ships 0.1.1, so Rain was
testing the glider slot from a loose jar in the instance rather than from the pack. Added at
0.1.1; release asset fetched and its sha512 matches the manifest.
