#!/bin/bash
# ONE-TIME, run on your Mac from the repo root: ./finalize-pack.sh
# Adds cupboard to the packwiz pack (the only CurseForge item not already in it —
# Cave Spelunking and Stay True are already tracked; only the .mrpack export
# excluded them, and git distribution has no such restriction), bumps the pack
# version, refreshes hashes, and makes the initial commit.
set -euo pipefail
cd "$(dirname "$0")"

# 1. Ensure packwiz is available
if ! command -v packwiz >/dev/null; then
  if command -v brew >/dev/null; then
    echo "Installing packwiz via Homebrew..."
    brew install packwiz
  elif command -v go >/dev/null; then
    go install github.com/packwiz/packwiz@latest
    export PATH="$PATH:$(go env GOPATH)/bin"
  else
    echo "Need packwiz: install Homebrew (brew.sh) then re-run"; exit 1
  fi
fi

# 2. Add cupboard (project 326652, file 8623564 = cupboard-fabric-1.21.1-4.0.jar)
cd pack
packwiz curseforge install --addon-id 326652 --file-id 8623564

# 3. Bump version and refresh index hashes
sed -i '' 's/^version = ".*"/version = "1.2.0"/' pack.toml
packwiz refresh
cd ..

# 4. Retire the now-redundant manual script and commit everything
[ -f setup-instance.sh ] && mv setup-instance.sh archive/scripts/
rm -f .git/index.lock   # leftover from sandboxed init
git add -A
git commit -m "1.2.0: rain settlement packwiz pack + launcher auto-update setup (adds cupboard)"
echo "Done. Now create the GitHub repo and push (commands from Claude)."
