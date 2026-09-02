# Plan: equipped gliders — make Gliding work from an Accessories slot

Written 2026-09-01 for handoff. Goal: glide by holding jump with **empty hands**, with a glider
equipped in an Accessories slot, instead of having to hold one.

**This plan is executable as written.** Sections 3 and 4 are already-verified findings and pinned
decisions — do not re-derive or re-litigate them. Section 5 lists what is genuinely still unknown.
Ask Benjamin before deviating from a Section 4 decision.

## 1. Success criteria

- 1a. `glider-accessory-<ver>.jar` builds from `glider-accessory/` with `./gradlew build`.
- 1b. In the `rain-settlement` Prism instance, any `gliding:*_glider` item can be placed in the
  Accessories **cape** slot (the Accessories screen, default keybind `V`).
- 1c. With a glider equipped and **both hands empty**, jumping off a cliff and holding space
  glides: forward speed increases, descent slows, and no fall damage is taken on landing.
- 1d. The glider model renders on the player's back while gliding (this is the mod's existing
  `GliderModelFeatureRenderer` behaviour and should follow for free — see 3d).
- 1e. Unequipping the glider mid-air ends the glide immediately; the player falls normally.
- 1f. Holding a glider in the main hand still works exactly as before (no regression).
- 1g. `logs/latest.log` shows the mixin applying and **no** `NullPointerException` from
  `com.l33tfox.gliding.networking.C2SPacketHandler` or `GliderUtil.playerGliderMovement`.

## 2. Environment

Rain Settlement pack 1.6.2 — Fabric loader 0.19.3, Minecraft 1.21.1, JVM target 21.
Build with `JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home`
(there is no system JDK on this Mac; `gradle.properties` also pins `org.gradle.java.home`).

| id | version | role |
|---|---|---|
| gliding | 1.1.0 | the glider items and all gliding logic — the mixin target |
| accessories | 1.1.0-beta.53+1.21.1 | the equipment slots and the capability API |
| trinkets | 3.10.0 | present, but **not** the integration target — see 4b |
| travelersbackpack | 10.1.38 | occupies the `back` slot; the reason we use `cape` — see 4c |
| aether | 1.5.11+openair.1 | also tags into `cape`; the accepted conflict — see 4c |

Instance mods folder (source of the local jars in Phase 1):
`~/Library/Application Support/PrismLauncher/instances/rain-settlement/minecraft/mods/`

**Before launching the game to test:** check no Open-Air Settlement instance is already running.
Prism shares a machine-level lock and a second launch will fail or clobber the first.

## 3. Already verified — do not re-investigate

Findings from disassembling `gliding-1.1.0.jar` and `accessories-fabric-1.1.0-beta.53+1.21.1.jar`
on 2026-09-01. All of these are confirmed, not assumed.

- 3a. **Gliding is CC0-1.0.** `fabric.mod.json` declares `"license": "CC0-1.0"`, sources at
  `github.com/L33tfx/gliding-mod-1.21`. No licensing constraint on patching it.
- 3b. **Every gate funnels through two static methods** on `com.l33tfox.gliding.util.GliderUtil`:
  - `public static boolean isHoldingGlider(Player)` — implemented as
    `player.isHolding(s -> s.getItem() instanceof GliderItem)`. Hands only.
  - `public static GliderItem getGliderItemInHand(Player)` — returns main-hand glider, else
    off-hand glider, else **null**.

  Callers: `GliderClientUtil.isActivatingGlider` (client activation),
  `GliderUtil.playerGliderMovement` (the actual physics, run on **both** sides),
  and `GliderModelFeatureRenderer` (the back-mounted render).
- 3c. **The server does not re-validate.** `C2SPacketHandler.receiveGliderActivated` trusts the
  client packet and calls `playerGliderMovement`, which dereferences
  `getGliderItemInHand(...).glideDropVelocity`. If that returns null the **server** throws.
  This is why the mod must be installed server-side too, not just on the client.
- 3d. **The render follows the same two methods.** `GliderModelFeatureRenderer` calls
  `isHoldingGlider` and `getGliderItemInHand`. Patching those two gets criterion 1d for free.
- 3e. **Gliding has no config hook for this.** Its only option is `offHandEnabled`
  (`GlidingSyncedConfig` / `GlidingServersideConfig`). There is no slot or accessory setting.
- 3f. **A datapack alone cannot do this.** A tag makes a glider *equippable*; nothing in Gliding
  reads equipped slots. The code change in Phase 2 is mandatory.
- 3g. **Accessories grants `cape` and `back` by default** to players
  (`data/accessories/accessories/entity/default.json`), each holding 1 item, with an
  `accessories:tag` validator.
- 3h. **The tag path is `data/accessories/tags/item/<slot>.json`.** Confirmed by three mods in
  this pack doing exactly this: Aether (`back`, `cape`), Traveler's Backpack (`back`).
  Format: `{"replace": false, "values": ["<item id>", ...]}`.
- 3i. **The Accessories read API** is
  `AccessoriesCapability.getOptionally(LivingEntity) -> Optional<AccessoriesCapability>`, then
  `getFirstEquipped(Predicate<ItemStack>) -> SlotEntryReference` (a record with `.stack()`).
- 3j. **The six glider item ids** are `gliding:wooden_glider`, `gliding:stone_glider`,
  `gliding:iron_glider`, `gliding:golden_glider`, `gliding:diamond_glider`,
  `gliding:netherite_glider`.
- 3k. **packwiz-installer leaves untracked jars alone** (`minecraft/packwiz.json` `cachedFiles`
  lists only pack-managed files), so hand-dropping a dev jar into the instance is safe.

## 4. Decisions already made — implement these, do not substitute

- 4a. **A new companion mod, not a fork of Gliding.** A ~40-line mixin mod leaves the pack's
  `gliding.pw.toml` entry untouched and stays updatable. Forking would mean replacing a pack mod
  with a locally built jar and re-porting on every Gliding update. Fork only if Phase 2 proves
  unworkable (see 8b).
- 4b. **Integrate with Accessories, not Trinkets.** Both are installed, but Accessories is what
  the pack's own mods target (Aether and Traveler's Backpack ship `data/accessories/` tags), and
  it owns the slots the player actually sees.
- 4c. **Use the `cape` slot.** It already exists, is already in the `chest` group, and already has
  a GUI icon — one JSON file, zero new slot infrastructure. `back` was rejected because Traveler's
  Backpack occupies it and it holds one item; a dedicated `glider` slot was rejected because it
  needs four data files plus group-merge behaviour this plan has not verified. The accepted cost:
  you cannot wear an Aether cape and a glider at once. Appendix A has the dedicated-slot recipe if
  Benjamin asks for it later.
- 4d. **Hand takes priority over the equipped slot.** If a glider is in either hand, use that one
  (it may be a different tier). The equipped glider is the fallback.
- 4e. **Do not add this to `mod/` (Lightning Expansion).** That mod hard-depends on
  spell_engine/spell_power/wizards and is not shipping yet; coupling would block this behind it.
- 4f. **Ship the item tag inside the mod jar**, not as a separate packwiz datapack. One artifact,
  and the tag can never be present without the code that makes it mean something.
- 4g. **Mod identity**: directory `glider-accessory/`, mod id `glider_accessory`, group
  `io.sadya`, license GPL-3.0, archivesName `glider-accessory`, starting version `0.1.0`.

## 5. Assumptions ledger — the real risks

| # | Assumption | Load-bearing? | Cheapest validation |
|---|---|---|---|
| 5a | A mixin into another mod's class applies with `remap = false` and name-only method selectors | **Yes** | Phase 3 launch. `latest.log` prints a mixin apply error naming `GliderUtilMixin` if not. See 8a for the fallback ladder. |
| 5b | `AccessoriesCapability.getFirstEquipped(Predicate)` excludes cosmetic-slot items | No | If a cosmetic glider enables gliding, pass `EquipmentChecking.ACCESSORIES_ONLY` to the two-arg overload. |
| 5c | Calling the capability every client tick is cheap enough | No | Accessories caches equipped lookups. If F3 shows a tick cost, cache the result per tick in a static field. |
| 5d | Gliding's `isActivatingGlider` reaches `isHoldingGlider` rather than `mainHandHoldingGlider` | **Yes** | It branches on `GlidingSyncedConfig.offHandEnabled`: true → `isHoldingGlider`, false → `mainHandHoldingGlider`. **Confirm `offHandEnabled` is true** in the instance's Gliding config before testing; if it is false, either turn it on or also inject into `mainHandHoldingGlider`. |
| 5e | The Open-Air Settlement server will accept a new mod | **Yes, for multiplayer** | Out of scope for this plan — see Section 9. Single-player works standalone. |

## 6. Phase 1 — scaffold the project

Create `glider-accessory/` as a sibling of `mod/`. Copy the build harness rather than
regenerating it, so the toolchain matches what already works on this Mac:

```bash
cd /Users/benji/Code/rain-settlement
mkdir -p glider-accessory/libs
cp -R mod/gradle mod/gradlew mod/gradlew.bat mod/.gitignore glider-accessory/
MODS=~/Library/Application\ Support/PrismLauncher/instances/rain-settlement/minecraft/mods
cp "$MODS/gliding-1.1.0.jar" "$MODS/accessories-fabric-1.1.0-beta.53+1.21.1.jar" glider-accessory/libs/
```

`libs/` is gitignored by the copied `.gitignore` — that is intended, matching `mod/`. Anyone
rebuilding from a fresh clone re-copies both jars from the instance.

**`glider-accessory/settings.gradle`**

```groovy
pluginManagement {
	repositories {
		maven { name = 'Fabric'; url = 'https://maven.fabricmc.net/' }
		mavenCentral()
		gradlePluginPortal()
	}
}
rootProject.name = 'glider-accessory'
```

**`glider-accessory/gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.parallel=true
org.gradle.configuration-cache=false
# No system JDK on this Mac; Homebrew openjdk@21.
org.gradle.java.home=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home

minecraft_version=1.21.1
loader_version=0.19.3
loom_version=1.17-SNAPSHOT

version=0.1.0
group=io.sadya

gliding_jar=gliding-1.1.0.jar
accessories_jar=accessories-fabric-1.1.0-beta.53+1.21.1.jar
```

**`glider-accessory/build.gradle`**

```groovy
plugins {
	id 'net.fabricmc.fabric-loom-remap' version "${loom_version}"
}

dependencies {
	minecraft "com.mojang:minecraft:${project.minecraft_version}"
	mappings loom.officialMojangMappings()
	modImplementation "net.fabricmc:fabric-loader:${project.loader_version}"

	// Compile against the exact pack jars. Both are copied from the Prism instance into libs/
	// (gitignored); neither is remapped away, since we only touch their own class names.
	modCompileOnly files("libs/${project.gliding_jar}")
	modCompileOnly files("libs/${project.accessories_jar}")
}

processResources {
	def version = project.version
	inputs.property "version", version
	filesMatching("fabric.mod.json") { expand "version": version }
}

tasks.withType(JavaCompile).configureEach { it.options.release = 21 }

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

base { archivesName = 'glider-accessory' }
```

No fabric-api dependency and no entrypoint: this mod is mixins and one data file. Do not add
either "for completeness".

## 7. Phase 2 — the code and data

Four files. Everything under `glider-accessory/src/main/`.

**`resources/fabric.mod.json`**

```json
{
	"schemaVersion": 1,
	"id": "glider_accessory",
	"version": "${version}",
	"name": "Glider Accessory",
	"description": "Lets Gliding's gliders work from an Accessories slot instead of the hand.",
	"authors": ["Benjamin"],
	"contact": { "sources": "https://github.com/benji-hix/rain-settlement" },
	"license": "GPL-3.0",
	"environment": "*",
	"mixins": ["glider_accessory.mixins.json"],
	"depends": {
		"fabricloader": ">=0.16.3",
		"minecraft": "~1.21.1",
		"java": ">=21",
		"gliding": "*",
		"accessories": "*"
	}
}
```

The `gliding` and `accessories` depends are **required**, not suggested: the mixin targets a class
that only exists when Gliding is present, and a missing target is a hard crash at load.

**`resources/glider_accessory.mixins.json`**

```json
{
	"required": true,
	"package": "io.sadya.glider_accessory.mixin",
	"compatibilityLevel": "JAVA_21",
	"mixins": ["GliderUtilMixin"],
	"injectors": { "defaultRequire": 1 }
}
```

Note this goes in `mixins`, not `client` — per 3c the physics runs on both sides.

**`resources/data/accessories/tags/item/cape.json`** (this is 4f — the tag ships in the jar)

```json
{
	"replace": false,
	"values": [
		"gliding:wooden_glider",
		"gliding:stone_glider",
		"gliding:iron_glider",
		"gliding:golden_glider",
		"gliding:diamond_glider",
		"gliding:netherite_glider"
	]
}
```

**`java/io/sadya/glider_accessory/mixin/GliderUtilMixin.java`**

```java
package io.sadya.glider_accessory.mixin;

import com.l33tfox.gliding.items.GliderItem;
import com.l33tfox.gliding.util.GliderUtil;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Gliding decides everything — client activation, the movement applied on both sides, and the
 * back-mounted render — from two static lookups that only ever read the player's hands. Widening
 * both to also see an equipped Accessories glider is the whole feature.
 */
@Mixin(value = GliderUtil.class, remap = false)
public abstract class GliderUtilMixin {

	@Inject(method = "isHoldingGlider", at = @At("HEAD"), cancellable = true)
	private static void glider_accessory$countEquipped(Player player, CallbackInfoReturnable<Boolean> cir) {
		if (equippedGlider(player) != null) {
			cir.setReturnValue(true);
		}
	}

	@Inject(method = "getGliderItemInHand", at = @At("HEAD"), cancellable = true)
	private static void glider_accessory$useEquipped(Player player, CallbackInfoReturnable<GliderItem> cir) {
		// 4d: a glider in hand wins — it may be a different tier than the equipped one.
		if (GliderUtil.mainHandHoldingGlider(player) || GliderUtil.offHandHoldingGlider(player)) {
			return;
		}
		GliderItem equipped = equippedGlider(player);
		if (equipped != null) {
			cir.setReturnValue(equipped);
		}
	}

	/** The GliderItem in one of this player's Accessories slots, or null if there is none. */
	private static GliderItem equippedGlider(Player player) {
		AccessoriesCapability capability = AccessoriesCapability.getOptionally(player).orElse(null);
		if (capability == null) {
			return null;
		}
		SlotEntryReference equipped = capability.getFirstEquipped(GliderUtilMixin::isGlider);
		return equipped == null ? null : (GliderItem) equipped.stack().getItem();
	}

	private static boolean isGlider(ItemStack stack) {
		return stack.getItem() instanceof GliderItem;
	}
}
```

Two things that are load-bearing and easy to get wrong:

- 7a. `remap = false` on `@Mixin` and **name-only** `method =` selectors (no descriptors). The
  target is another mod's class, whose own names are never remapped; a descriptor would need
  intermediary Minecraft types and would silently fail to match in production. The `Player`
  parameter in the handler signatures is still remapped correctly by Loom at `remapJar` time —
  that is a separate mechanism from the mixin selector.
- 7b. The injected handlers must be `static`, because the targets are static.

## 8. Phase 3 — build, install, test

```bash
cd /Users/benji/Code/rain-settlement/glider-accessory && ./gradlew build
```

Output lands at `build/libs/glider-accessory-0.1.0.jar`. Install it for testing by copying into
the instance (safe per 3k):

```bash
cp glider-accessory/build/libs/glider-accessory-0.1.0.jar ~/Library/Application\ Support/PrismLauncher/instances/rain-settlement/minecraft/mods/
```

Then, in a **creative** single-player test world:

1. Confirm 5d first: check Gliding's config (Mod Menu → Gliding, or `config/gliding*.json*`) has
   off-hand enabled. If it is off, turn it on before anything else.
2. `/give @s gliding:iron_glider`, open the Accessories screen, place it in the cape slot.
3. Empty both hands. Switch to survival, jump from height, hold space. → criteria 1c, 1d.
4. Land. Confirm no fall damage. → 1c.
5. Glide again and unequip mid-air. → 1e.
6. Put a glider back in the main hand with the cape slot empty and glide. → 1f.
7. `grep -iE "glider_accessory|GliderUtilMixin|Exception" logs/latest.log`. → 1g.

Also run one **integrated-server** check specifically for 3c: gliding in single-player already
exercises the server path, so a clean log there covers it.

Fallback ladder if the mixin does not apply (5a) — try in order, stopping at the first that works:

- 8a. Log says the mixin target class was not found → Gliding is not loading, or the `depends`
  block is wrong. Check the mod list in-game first.
- 8b. Log says the injection point was not found, or the method selector matched nothing → try
  `remap = true`, then a fully-qualified selector. If neither works, escalate to Benjamin before
  falling back to 4a's rejected option (forking `github.com/L33tfx/gliding-mod-1.21`, which is CC0
  and therefore permitted, but is a last resort).
- 8c. Gliding works but no glider renders on the back → 1d has regressed to a nice-to-have, not a
  blocker. Report it rather than chasing it.

## 9. Phase 4 — publishing (confirm with Benjamin before doing this)

Do **not** publish without asking. Two separate steps, and the second is not this repo's call:

- 9a. **Rain Settlement pack**: commit the built jar under `pack/mods/`, `packwiz refresh`, bump
  `version` in `pack/pack.toml`, commit and push. Follow whatever the Lightning Expansion plan
  settled on for shipping a locally built jar through packwiz (assumption 3h there).
- 9b. **Open-Air Settlement server**: per 3c this mod is required server-side for multiplayer
  gliding — without it the server throws on the movement packet. That is a change to someone
  else's server and is **out of scope**. Flag it to Benjamin; do not contact the server operator.

## 10. Out of scope

Balance changes to glider tiers, a crafting recipe change, glider durability, making gliders
work in the `back` slot alongside a backpack, and any Trinkets-side integration.

## Appendix A — dedicated `glider` slot, if 4c is overturned

Only if Benjamin asks. Replaces the single tag file with four:

- `data/glider_accessory/accessories/slot/glider.json` —
  `{"replace": false, "amount": 1, "operation": "set", "order": 810, "icon": "accessories:gui/slot/back", "validators": ["accessories:tag", "accessories:component"]}`
- `data/glider_accessory/accessories/entity/glider.json` —
  `{"replace": false, "entities": ["#accessories:defaulted_targets"], "slots": ["glider"]}`
- `data/glider_accessory/accessories/group/chest.json` —
  `{"replace": false, "slots": ["glider"]}` (**unverified**: that `replace: false` appends to
  Accessories' own `chest` group rather than needing the full slot list restated. Verify in the
  Accessories screen; if the slot does not appear, restate all of `necklace`, `cape`, `back`,
  `glider` and set the group's `order` to `80` to match.)
- `data/accessories/tags/item/glider.json` — the same six item ids as in Phase 2.

Reusing `accessories:gui/slot/back` as the icon avoids needing new art.

---

## Status — 2026-09-02

Executed and verified in-game. All of section 1's criteria pass: the glider equips to the
Accessories cape slot, gliding works hands-empty with the model rendering, unequipping mid-air
drops the player normally, holding a glider still works unchanged, and `latest.log` shows no
`GliderUtilMixin` error and no NPE from `C2SPacketHandler` or `playerGliderMovement`.

Two decisions from section 4 were superseded after the fact, at Benjamin's direction:

- **4g (mod identity) is retired.** The mod is named **Gliding Accessories** — mod id
  `gliding_accessories`, package `io.sadya.gliding_accessories`, archivesName
  `gliding-accessories`. `glider-accessory` / `glider_accessory` exist nowhere any more.
- **The mod no longer lives in this repo.** It is its own project at
  `~/Hosted-Materials/Gliding-Accessories`, pushed to
  https://github.com/benji-hix/Gliding-Accessories (public). Section 9a's "commit the built jar
  under `pack/mods/`" is therefore the only remaining pack-side step, and it now consumes a
  release artifact from that repo rather than a jar built inside this one.

Section 5's assumptions resolved: 5a holds (the mixin applies and its handlers ran every client
tick for a full session with no error); 5d confirmed (`config/gliding.json` has
`offHandEnabled: true`); 5b and 5c were never exercised and remain unverified. 5e is unchanged —
see section 9b, still Benjamin's call and the server operator's.

Unrelated but discovered here: the JVM native crashes are a bundled-JDK problem, not this mod's.
See ledger section 8a.
