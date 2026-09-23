#!/usr/bin/env bash
# Bump version, run tests + lint, build the signed release AAB + APK, and
# optionally publish (commit + tag + push) so GitHub Pages goes live.
# Usage: ./release.sh [major|minor|patch] [--publish]
#   --publish commits version.properties + docs, tags vX.Y.Z and pushes.
set -euo pipefail
cd "$(dirname "$0")"

PART="patch"
PUBLISH=0
for arg in "$@"; do
  case "$arg" in
    major|minor|patch) PART="$arg" ;;
    --publish) PUBLISH=1 ;;
    *) echo "usage: $0 [major|minor|patch] [--publish]" >&2; exit 1 ;;
  esac
done

./gradlew -q bumpVersion -PversionPart="$PART"
VERSION=$(./gradlew -q printVersion)
CODE=$(./gradlew -q printVersionCode)
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Android/Sdk}"

echo "==> Building release v$VERSION (versionCode $CODE)"
./gradlew testDebugUnitTest lintDebug bundleRelease assembleRelease

echo
echo "==> OWASP dependency scan (CVEs in app dependencies)"
SCAN_LOG="${TMPDIR:-/tmp}/dependency-check-$VERSION.log"
if ./gradlew dependencyCheckAnalyze --console=plain >"$SCAN_LOG" 2>&1; then
  echo "  Clean: no known vulnerabilities at or above CVSS 9."
else
  echo "  !! Issues found by the scan - see app/build/reports/dependency-check/ (report HTML + XML)."
  echo "     Scan log: $SCAN_LOG"
  tail -n 15 "$SCAN_LOG"
fi
echo

echo
echo "Released v$VERSION (versionCode $CODE)"
echo "  APK: app/build/outputs/apk/release/app-release.apk"
echo "  AAB: app/build/outputs/bundle/release/app-release.aab"

mkdir -p "releases/v$VERSION"
cp -f "app/build/outputs/apk/release/app-release.apk" "releases/v$VERSION/spartan-launcher-v$VERSION.apk"
cp -f "app/build/outputs/bundle/release/app-release.aab" "releases/v$VERSION/app-release.aab"
echo
echo "Artifacts preserved in releases/v$VERSION/"

# Sync the GitHub Pages download page.
mkdir -p "docs/downloads"
cp -f "app/build/outputs/apk/release/app-release.apk" "docs/downloads/spartan-launcher-v$VERSION.apk"
cp -f "PRIVACY_POLICY.md" "docs/PRIVACY_POLICY.md"
printf '{"versionName":"%s","versionCode":%s,"released":"%s"}\n' "$VERSION" "$CODE" "$(date +%F)" > "docs/version.json"
# Point every download link in the landing page at the versioned file.
sed -i "s|downloads/spartan-launcher-v[0-9.]*\\.apk|downloads/spartan-launcher-v$VERSION.apk|g" "docs/index.html"
echo "Download page synced (docs/downloads/spartan-launcher-v$VERSION.apk, docs/version.json)"

if [ "$PUBLISH" = "1" ]; then
  git add version.properties docs
  git commit -m "chore(release): v$VERSION" >/dev/null 2>&1 || echo "Nothing new to commit (release already committed)."
  git tag -a "v$VERSION" -m "v$VERSION" 2>/dev/null || echo "Tag v$VERSION already exists."
  git push origin main
  git push --tags
  echo
  echo "Published v$VERSION"
  echo "Live: https://sifytul.github.io/spartan-launcher/"
else
  echo "Remember (or re-run with --publish):"
  echo "  git add version.properties docs && git commit -m \"chore(release): v$VERSION\""
  echo "  git tag -a v$VERSION -m \"v$VERSION\""
  echo "  git push origin main && git push --tags"
fi