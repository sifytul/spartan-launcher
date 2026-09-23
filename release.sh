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

# Sync the GitHub Pages download page.
mkdir -p "docs/downloads"
cp -f "app/build/outputs/apk/release/app-release.apk" "docs/downloads/app-release.apk"
cp -f "PRIVACY_POLICY.md" "docs/PRIVACY_POLICY.md"
printf '{"versionName":"%s","versionCode":%s,"released":"%s"}\n' "$VERSION" "$CODE" "$(date +%F)" > "docs/version.json"
echo "Download page synced (docs/downloads/app-release.apk, docs/version.json)"
echo "Remember:"
echo "  git add version.properties"
echo "  git commit -m \"chore(release): v$VERSION\""
echo "  git tag -a v$VERSION -m \"v$VERSION\""