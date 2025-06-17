#!/bin/bash

BUILD_NATIVE_DIR="build-native"
CLASSPATH="../lib/antlr-4.7-complete.jar:.:../build-tile"  # use forward slashes on Linux

mkdir -p "$BUILD_NATIVE_DIR"
cd "$BUILD_NATIVE_DIR"

NATIVE_IMAGE="/home/yasinxdxd/.sdkman/candidates/java/current/bin/native-image"

# Optional: print what you're doing
echo "Using native-image from: $NATIVE_IMAGE"
echo "Using classpath: $CLASSPATH"

# Run native-image
"$NATIVE_IMAGE" \
  -H:+UnlockExperimentalVMOptions \
  -H:-CheckToolchain \
  -cp "$CLASSPATH" \
  tile.app.Tile \
  -o "tile"
