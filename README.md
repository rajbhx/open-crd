# open-crd

[![ci](https://github.com/rajbhx/open-crd/actions/workflows/ci.yml/badge.svg)](https://github.com/rajbhx/open-crd/actions/workflows/ci.yml)
[![android](https://github.com/rajbhx/open-crd/actions/workflows/android.yml/badge.svg)](https://github.com/rajbhx/open-crd/actions/workflows/android.yml)

Open-source rebuild of the two shipped Chrome Remote Desktop artifacts, in the
form each side actually needs:

| Side | Deliverable | Language | Reference artifact | Version |
|---|---|---|---|---|
| Receiver (client) | **Android app — APK** | Kotlin — `receiver-android/` | `com.google.chromeremotedesktop_79.0.3945.26...apk` | 79.0.3945.26 |
| Receiver (desktop reference) | Windows client | C/C++ — `receiver-cpp/` | (same APK, protocol reference) | — |
| Host (Windows) | Windows service/CLI | Rust — `host-rs/` | `chromeremotedesktophost.msi` | 154.0.8037.40 |

The rebuild is driven by REA evidence extracted from those artifacts
(see [`docs/EVIDENCE.md`](docs/EVIDENCE.md) and [`docs/PROTOCOL.md`](docs/PROTOCOL.md)).

No Google binaries are redistributed in this repository — the reference
artifacts are never committed. The Android receiver contains **no NDK code**
(pure Kotlin/JVM, no native toolchain) and **no Google Play services**: it is
original code that talks directly to public services, so the signature checks
that reject re-signed Google APKs do not apply to it. Connection methods:
**SSH (live today)** and Google account / Chrome Remote Desktop (next
milestones).

## Status — v0.2

- [x] Open-source charter + MIT license
- [x] Reference artifact inventory + REA evidence index
- [x] Host scaffold (Rust, zero dependencies) — builds in CI
- [x] Desktop receiver scaffold (C/C++, CMake) — builds in CI
- [x] **Android receiver (Kotlin) — debug APK on every push, signed release APK on tags**
- [x] Receiver 0.2: SSH sign-in + interactive shell
- [ ] Receiver 0.3: Google sign-in (OAuth) + host directory
- [ ] Receiver 0.4: FTL signaling
- [ ] Receiver 0.5: WebRTC session (video/input)
- [ ] Protocol dossier (host registration, FTL signaling, session transport)
- [ ] Host: registration, signaling, session

## Releases

APKs are published under [Releases](https://github.com/rajbhx/open-crd/releases):
`opencrd-receiver-vX.Y.Z.apk` (signed) with `SHA256SUMS.txt`. Install via
`adb install` or sideload after allowing installation from unknown sources.
Details: [`receiver-android/README.md`](receiver-android/README.md).

## Layout

```
receiver-android/  Android receiver (Kotlin) — the APK deliverable
receiver-cpp/      desktop reference receiver (C/C++, CMake)
host-rs/           Rust Windows host — Cargo project, zero dependencies
docs/              protocol dossier + evidence index
.github/           CI workflows
```

Planned receiver modules (details in each subproject README):
`config`, `auth`, `directory`, `ftl` (signaling), `session` (transport).

## Builds

All builds run in CI (GitHub Actions) — see
[Actions](https://github.com/rajbhx/open-crd/actions).

- [`android.yml`](.github/workflows/android.yml) — receiver APK (Gradle +
  Android SDK, **no NDK**), artifact on every push
- [`release.yml`](.github/workflows/release.yml) — on tag `v*`: signed release
  APK published as a GitHub Release
- [`ci.yml`](.github/workflows/ci.yml) — C++ desktop receiver (UCRT64) and
  Rust host (static CRT), both smoke-run

## Legal

MIT-licensed original code, not affiliated with or endorsed by Google.
Protocol facts were derived from operator-supplied binaries for interoperability
and personal/experimental use. Connect only to accounts and machines you own.
