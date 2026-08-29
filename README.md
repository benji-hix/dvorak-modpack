# rain settlement

Fabric 1.21.1 modpack, distributed via [packwiz](https://packwiz.infra.link/). The
Prism instance syncs itself to this repo on every launch — no manual updating.

## One-time setup (per machine)

1. **Create the instance** in Prism Launcher (skip if you already have one):
   Add Instance → Custom → name it `rain-settlement` → Minecraft **1.21.1** → Mod Loader:
   **Fabric** (latest loader is fine). Don't add any mods.
2. **Quit Prism** (it overwrites `instance.cfg` on exit).
3. From this repo's folder:

   ```
   ./setup-launcher.sh            # or: ./setup-launcher.sh <instance-name>
   ```

   This drops `packwiz-installer-bootstrap.jar` into the instance, backs up any
   unmanaged `mods/` folder, and sets the pre-launch command to:

   ```
   "$INST_JAVA" -jar packwiz-installer-bootstrap.jar https://raw.githubusercontent.com/benji-hix/rain-settlement/main/pack/pack.toml
   ```

4. **Launch.** First launch downloads all mods; later launches sync in seconds.
   If a CurseForge author blocks automated downloads, the installer shows a
   one-time manual-download prompt.

## Publishing an update

```
cd pack
packwiz modrinth install <slug>          # add a mod (or: packwiz curseforge install <slug>)
packwiz update --all                     # or update everything
packwiz remove <name>                    # remove a mod
```

Then bump `version` in `pack.toml`, and:

```
packwiz refresh
git add -A && git commit -m "x.y.z: what changed" && git push
```

Every instance picks up the change on its next launch.

## Repo layout

- `pack/` — packwiz root (`pack.toml`, `index.toml`, mod/resourcepack metadata)
- `setup-launcher.sh` — one-time per-machine instance wiring
- `finalize-pack.sh` — historical one-time migration (cupboard + initial commit)
