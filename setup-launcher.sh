#!/bin/bash
# ONE-TIME per machine: wires a Prism instance to auto-update from the GitHub pack.
# Usage: ./setup-launcher.sh [instance-name]   (default: dvorak)
# After this, just launch the instance — every launch syncs mods/configs to the repo.
set -euo pipefail

GITHUB_USER="benji-hix"
PACK_URL="https://raw.githubusercontent.com/$GITHUB_USER/dvorak-modpack/main/pack/pack.toml"
BOOTSTRAP_URL="https://github.com/packwiz/packwiz-installer-bootstrap/releases/latest/download/packwiz-installer-bootstrap.jar"

pgrep -qi prismlauncher && { echo "Quit Prism Launcher first (it overwrites instance.cfg on exit)."; exit 1; }

INSTANCE="${1:-dvorak}"
BASE="$HOME/Library/Application Support/PrismLauncher/instances/$INSTANCE"
MCDIR=""
for d in "$BASE/minecraft" "$BASE/.minecraft"; do [ -d "$d" ] && MCDIR="$d" && break; done
[ -z "$MCDIR" ] && { echo "Instance '$INSTANCE' not found under $BASE"; exit 1; }

# 1. Bootstrap jar into the game dir (pre-launch commands run with CWD = game dir)
curl -fsSL -o "$MCDIR/packwiz-installer-bootstrap.jar" "$BOOTSTRAP_URL"
echo "bootstrap jar installed"

# 2. Back up unmanaged mods so the packwiz installer starts from a clean slate.
#    (First sync re-downloads everything the pack defines; old jars would duplicate.)
if [ -d "$MCDIR/mods" ] && [ -n "$(ls -A "$MCDIR/mods" 2>/dev/null)" ]; then
  BK="$MCDIR/mods.backup-$(date +%Y%m%d-%H%M%S)"
  mv "$MCDIR/mods" "$BK" && mkdir "$MCDIR/mods"
  echo "existing mods moved to $(basename "$BK") (delete it once the pack launches fine)"
fi

# 3. Set the pre-launch command in instance.cfg
CFG="$BASE/instance.cfg"
CMD="\"\$INST_JAVA\" -jar packwiz-installer-bootstrap.jar $PACK_URL"
python3 - "$CFG" "$CMD" <<'EOF'
import sys, configparser
cfg_path, cmd = sys.argv[1], sys.argv[2]
cp = configparser.ConfigParser(interpolation=None)
cp.optionxform = str
with open(cfg_path) as f:
    text = f.read()
has_section = text.lstrip().startswith('[')
cp.read_string(text if has_section else '[General]\n' + text)
sec = cp.sections()[0]
cp[sec]['OverrideCommands'] = 'true'
cp[sec]['PreLaunchCommand'] = cmd
with open(cfg_path, 'w') as f:
    cp.write(f, space_around_delimiters=False)
EOF
echo "pre-launch command set. Launch '$INSTANCE' from Prism — it will sync from GitHub every launch."
