# Spartan Launcher

A minimalist, distraction-free home screen for Android. Built to help you use
your phone less: block distracting apps, set daily time limits, run
pomodoro-style focus sessions, schedule auto-blocking windows, and mute
notifications — all with data that stays on your device.

No ads. No trackers. No accounts.

## Features

- Minimalist home: clock, date, favorites list
- Full-screen blocked-app screen with countdown and "use anyway (2 min)" grace
- Per-app daily time limits plus a screen-time report (Usage Stats)
- Focus sessions (15/25/45/60 min) with 5-minute breaks and end-of-session
  reminders; favorites remain usable as an allowlist
- Blocking schedules: auto-block chosen apps inside repeating time windows
- Notification filtering: mute chosen apps, or quiet everything during focus
- Per-app rename, font family / text-size options, and a monochrome
  (grayscale) theme
- Work-profile (managed) apps surfaced in the drawer where present
- Privacy-first: everything is computed on-device, nothing is collected

## Requirements

- Android 8.0 (API 26) or newer
- Accessibility + Usage Access + Notification Access / overlay grants are
  optional but unlock the blocking and screen-time features

## Build

Requirements: JDK 21, Android SDK (compileSdk 36). `local.properties` must
point at your SDK.

```bash
./gradlew assembleDebug                  # debug APK
./gradlew testDebugUnitTest              # unit tests
./gradlew lintDebug                      # lint
./gradlew bundleRelease                 # signed release AAB (needs local keystore)
```

Release versioning is automated (see `version.properties`, `release.sh`):

```bash
./release.sh            # bump patch (or `major` / `minor`), tests + lint, build AAB + APK
```

Signing keys (`keystore.properties`, `keystore/`) are gitignored — they never
leave your machine. Keep them backed up; losing them breaks updates.

## Download

Release APKs for direct install are published on the GitHub Pages site:
**https://sifytul.github.io/spartan-launcher/** (served from `docs/`).

## Privacy

The app requests Accessibility, Usage Access, Notification Access and
Display-over-apps permissions for its core features. All processing happens
on-device. See [`docs/PRIVACY_POLICY.md`](docs/PRIVACY_POLICY.md).

## Security

- **Dependency scan (OWASP Dependency-Check)**: `release.sh` runs
  `dependencyCheckAnalyze`, matching every app dependency against the NVD CVE
  database. It fails the scan on critical findings (CVSS >= 9); the HTML/XML
  report lands in `app/build/reports/dependency-check/`. The scan needs a free
  NVD API key: export `NVD_API_KEY=...` (or pass `-PnvdApiKey=...`) — get one at
  https://nvd.nist.gov/developers/request-an-api-key
- **CodeQL**: a GitHub Actions workflow (`.github/workflows/codeql-analysis.yml`)
  runs static analysis on every push to `main` and weekly. Results appear under
  **Security → Code scanning**.
- **Dependabot**: `.github/dependabot.yml` opens weekly PRs for outdated Gradle
  dependencies and GitHub Actions.

## License

Copyright 2026 sifytul

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.