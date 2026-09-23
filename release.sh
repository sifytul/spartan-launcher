#!/usr/bin/env bash
# Bump version, run tests + lint, and build the signed release AAB + APK.
# Usage: ./release.sh [major|minor|patch]   (default: patch)
set -euo pipefail
cd "$(dirname "$0")"

PART="${1:-patch}"
case "$PART" in
  major|minor|patch) ;;
  *) echo "usage: $0 [major|minor|patch]" >&2; exit 1 ;;
esac

./gradlew -q bumpVersion -PversionPart="$PART"
VERSION=$(./gradlew -q printVersion)
CODE=$(./gradlew -q printVersionCode)
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"

echo "==> Building release v$VERSION (versionCode $CODE)"
./gradlew testDebugUnitTest lintDebug bundleRelease assembleRelease

echo
echo "Released v$VERSION (versionCode $CODE)"
echo "  APK: app/build/outputs/apk/release/app-release.apk"
echo "  AAB: app/build/outputs/bundle/release/app-release.aab"

mkdir -p "releases/v$VERSION"
cp -f "app/build/outputs/apk/release/app-release.apk" "releases/v$VERSION/"
cp -f "app/build/outputs/bundle/release/app-release.aab" "releases/v$VERSION/"
echo
echo "Artifacts preserved in releases/v$VERSION/"
echo "Remember:"
echo "  git add version.properties"
echo "  git commit -m \"chore(release): v$VERSION\""
echo "  git tag -a v$VERSION -m \"v$VERSION\""