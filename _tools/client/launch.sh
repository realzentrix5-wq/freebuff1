#!/usr/bin/env bash
# Launch the headless client from _tools/client/fetch.py's download directory.
# Needs Xvfb on :99 (and openbox for keyboard input) plus mesa llvmpipe.
set -euo pipefail

DIR=${1:-/tmp/mc}
SERVER=${2:-127.0.0.1:25565}
NAME=${3:-Dev}

cd "$DIR"
cat > options.txt <<'EOF'
version:3955
lang:en_us
renderDistance:4
graphicsMode:0
guiScale:2
pauseOnLostFocus:false
resourcePacks:[]
incompatibleResourcePacks:[]
fullscreen:false
EOF

CP="client.jar:$(find libraries -name '*.jar' | tr '\n' ':')"
exec java -Xmx2G -Djava.library.path="$DIR/natives" -cp "$CP" \
  net.minecraft.client.main.Main \
  --username "$NAME" --version 1.21.11 --gameDir "$DIR" \
  --assetsDir "$DIR/assets" --assetIndex 29 \
  --accessToken 0 --userType legacy --versionType release \
  --width 854 --height 480 \
  --quickPlayMultiplayer "$SERVER"
