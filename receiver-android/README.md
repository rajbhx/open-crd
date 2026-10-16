# receiver-android — the receiver APK

The receiver deliverable: an installable Android app (APK) that connects to
remote machines. Clean-room Kotlin implementation, driven by the REA evidence
in [`../docs`](../docs).

## Why Kotlin (and not C/C++)

Project constraints, in priority order:

1. **The receiver must ship as an APK** — installable on Android.
2. **No Android NDK** — no native Android toolchain, ever.

An APK that contains C/C++ code can only be produced with the Android NDK.
Since the NDK is off-limits, the receiver is implemented in Kotlin with zero
native code. This also has practical benefits:

- **No Google Play services**: the app never calls Google Play APIs, so the
  signature checks that reject re-signed Google APKs do not apply to it. (The
  previous attempt failed because it modified Google's own binary; this app is
  original code talking to public network services.)
- **No native libraries at all**: the APK is pure JVM bytecode, so it runs on
  every device ABI (arm64, armv7, x86_64, x86) without a single `.so`.
- The SSH client is the pure-Java JSch fork (`com.github.mwiede:jsch`), and
  the future WebRTC milestone (0.5) will consume a prebuilt libwebrtc AAR from
  Maven (`io.github.webrtc-sdk:android`). Consuming prebuilt libraries is not
  building with the NDK.

## Modules (mirrors `docs/PROTOCOL.md`)

| Module | State | Notes |
|---|---|---|
| `Endpoints` | done | public signaling/directory endpoints from the evidence |
| `NetProbe` | done | DNS + TCP + TLS reachability self-test |
| `ssh` | done (0.2) | SSH sign-in + interactive shell (pure-Java JSch, no NDK) |
| `auth` | roadmap 0.3 | Google OAuth device flow (no Play services) |
| `directory` | roadmap 0.3 | host list for the signed-in account |
| `ftl` | roadmap 0.4 | signaling: protobuf over TLS to `instantmessaging-pa` |
| `session` | roadmap 0.5 | WebRTC: ICE/DTLS-SRTP, video decode, input channel |

## SSH sign-in (v0.2.0)

Any machine with an SSH server can be reached: Windows (OpenSSH Server
feature), Linux, macOS, NAS boxes, routers.

- Auth: password, or a pasted private key (OpenSSH/PEM text; RSA and ECDSA
  keys work out of the box — ed25519-only servers would need Bouncy Castle,
  which can be added later if needed).
- Host keys use trust-on-first-use: the first key seen for an address is
  pinned; a later change is refused until you use “Forget saved host key” on
  the connect screen.
- No password is stored by the app; host/port/username are remembered as
  convenience defaults only.
- The shell is a basic terminal: line input + output view with ANSI escape
  stripping (good for commands; not a full VT emulator yet).

Server side (Windows): Settings → Optional features → “OpenSSH Server”, then
`Start-Service sshd` (and `Set-Service sshd -StartupType Automatic`). Allow
password authentication, or install your public key into
`~/.ssh/authorized_keys`.

## Build

CI only (project rule): [`.github/workflows/android.yml`](../.github/workflows/android.yml)
builds the debug APK on every push; [`.github/workflows/release.yml`](../.github/workflows/release.yml)
builds the **signed** release APK on tags `v*` and publishes it under Releases.

Pinned toolchain: Gradle 8.10.2, JDK 17, Android Gradle Plugin 8.7.3,
Kotlin 2.0.21, compileSdk 35, minSdk 26 (Android 8.0+).

Developers who want to build outside CI: `gradle :app:assembleDebug` requires
a local Android SDK (SDK Platform 35, Build-Tools 35.0.0). No NDK is needed —
by design.

## Signing

Release APKs are signed with the project release key. The keystore and its
passwords live in GitHub Actions secrets (`ANDROID_KEYSTORE_BASE64`,
`ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`);
a backup copy is kept with the operator. Never commit the keystore.

## Install

1. Download `opencrd-receiver-vX.Y.Z.apk` from the repository Releases page.
2. `adb install opencrd-receiver-vX.Y.Z.apk`, or sideload the APK on the
   device (allow installation from unknown sources).
3. Verify integrity against the attached `SHA256SUMS.txt`.

## Version log

- **0.2.0** — SSH sign-in: password/private-key auth, TOFU host-key pinning,
  interactive shell.
- **0.1.0** — foundation: endpoint self-test (DNS/TCP/TLS) + diagnostics.
