# Powerbase for Android

**Your app's diagnostics and usage logs, on your own server — and in your pocket.**

Powerbase is a self-hosted telemetry stack. The server ingests batched events,
logs and crashes from Android apps and backend services into a single SQLite
file; this repository is the **Android client** that puts that data in front of
you: dashboards, a log explorer, crash groups, devices and alerts.

Kotlin + Jetpack Compose. No analytics SDK, no third-party service, no data
leaving your infrastructure.

**Public page:** https://merry8989-uk.github.io/powerbase-app/

---

## What it does

| Screen | What it shows |
|---|---|
| **Overview** | Active devices, sessions, events, crashes, crash-free rate and error-log count for the selected range, plus an events-per-day chart, top screens, top events and recent crashes. |
| **Charts** | The same series as a larger chart, with a metric picker (events / sessions / crashes / error logs) and a breakdown by event name and screen. |
| **Log explorer** | Every log line the server holds, filtered by level (DEBUG…FATAL) or searched full-text. Tap a row to read the stacktrace. |
| **Crash groups** | Crashes grouped by fingerprint, ranked by how often each one happened, with the device count and first/last seen. |
| **Devices** | Each install: manufacturer, model, Android version, app version, session count and last-seen time. |
| **Alerts** | Whatever the server's alert rules are firing — crash-free rate, error-log rate, a silent SDK, or a failed or stale maintenance job. |
| **Settings** | Theme, accent, text size, density, language, server connection, refresh cadence, per-screen switches, updates, and diagnostics. |

## Install

Grab the latest signed APK from
[Releases](https://github.com/merry8989-uk/powerbase-app/releases) and open it.
The app checks for updates itself — you never need a browser.

Requires Android 8.0 (API 26) or newer.

## Demo mode

The app works before you have a server. **Demo mode is on by default** and
shows built-in sample data, so every screen is explorable straight after
install. Turn it off in Settings and point the app at your own Powerbase
server:

1. Run the Powerbase server (see the server repository) and register an app:
   `python scripts/bootstrap.py --app-id com.example.shop --name "Example Shop"`
2. It prints two keys. Use the **read** key — an ingest key is write-only and
   cannot read data back.
3. In the app: Settings → Server → turn demo mode off → paste the server URL
   and the read key → **Test connection**.

## Privacy

- The app talks only to the server URL you configure.
- It sends a **read-scoped** key. That key can read your data and nothing else.
- Diagnostics logs are stored on-device and are only shared when you tap share
  or export.
- No analytics, no crash reporting to a third party, no ads, no tracking.

## Building

```bash
./gradlew assembleDebug     # debug build
./gradlew assembleRelease   # signed release (needs the env vars below)
```

Release signing reads from the environment, so no key material is ever in the
repository:

```
KEYSTORE_PATH, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
```

CI does this automatically on a `v*` tag and publishes the signed APK and AAB
as a GitHub Release.

## Contact

Feature requests, bug reports and suggestions: **[@osintgram_io](https://instagram.com/osintgram_io)** on Instagram.

## Licence

MIT. See [LICENSE](LICENSE).
