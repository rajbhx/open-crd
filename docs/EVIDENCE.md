# Evidence index

Every fact used in this repository is derived from the operator-supplied
artifacts below, analysed with REA (and standard package tooling for
extraction). Evidence IDs refer to the REA ledger of the analysis session.

## Receiver artifact (Android APK — used as protocol evidence only)

- Path: `C:\Users\raj\Downloads\com.google.chromeremotedesktop_79.0.3945.26-394502630_minAPI19(arm64-v8a)(nodpi)_apkmirror.com(1).apk`
- SHA-256: `F78A865955AC05C9CA5BB5C0014568575DF73A8E9693B649945333B59B75E7A9`
- Package: `com.google.chromeremotedesktop`, versionName `79.0.3945.26`, versionCode `394502630`, minSdk 19 / targetSdk 28
- Native engine: `lib/arm64-v8a/libremoting_client_jni.so` — 9,181,376 bytes,
  SHA-256 `FDF043CDB71A67F200141DB50AE39681FA14A7236DB1ED9A90A68A9B2AA8C6A9`
- JNI bridge (Java side): `org.chromium.chromoting.jni.*`
- REA evidence IDs (session ledger): `ev_c3058497b2266be97c77d7114ce5c732dc20efea4292e13666a2c6c64621d54c` (resource decode), `ev_307947c3e8cc75e07f7035d7b68c71310f9b78074dd3d1c05701cb6dc20e09a9` (package inspect)

## Host artifact (Windows MSI)

- Path: `C:\Users\raj\Downloads\chromeremotedesktophost.msi`
- SHA-256: `C7F6FB29BB6DA0DDAD9A847E4A4C353AB20B255D1A6AB60978285673DC57D10B`
- Product version: **154.0.8037.40** (extracted tree: `Google\Chrome Remote Desktop\154.0.8037.40\`)
- Extraction method: `msiexec /a <msi> /qn TARGETDIR=<tmp>` (administrative install, extract-only)
- Contents (19 files):

| File | Bytes | Role |
|---|---|---|
| `remoting_core.dll` | 33,905,816 | host engine (Chromium remoting host core) |
| `icudtl.dat` | 10,819,840 | ICU data |
| `remoting_crashpad_handler.exe` | 3,244,184 | crash reporting |
| `remoting_native_messaging_host.exe` | 482,968 | browser native-messaging bridge |
| `remote_assistance_host_uiaccess.exe` | 481,944 | IT2Me host (UIAccess variant) |
| `remote_open_url.exe` | 480,920 | URL opener helper |
| `remote_assistance_host.exe` | 480,408 | IT2Me host |
| `remote_security_key.exe` | 477,848 | securid/key helper |
| `remote_webauthn.exe` | 477,336 | WebAuthn helper |
| `remoting_start_host.exe` | 477,336 | host starter/utility |
| `remoting_desktop.exe` | 76,440 | desktop capture helper |
| `remoting_host.exe` | 74,392 | host launcher |
| `com.google.chrome.remote_*.json` (5×) | ≤341 | Chrome/Firefox native-messaging manifests |
| nested `chromeremotedesktophost.msi` | 372,736 | component payload |

REA evidence IDs (session ledger): recorded in `docs/PROTOCOL.md` per finding.

## Derived analysis artifacts (session working files)

- `strings_remoting_core.txt` — 44,891 unique printable strings (≥ 8 chars)
  swept from `remoting_core.dll` (33,905,816 bytes; sha256 `a0065892...`).
  Basis of the protocol findings in `docs/PROTOCOL.md`. Stored outside the
  repository as a working artifact.
- REA availability note: the REA MCP tool catalog was dropped by a host restart
  mid-session (only base tools remain; `rea doctor` diagnosis was itself
  interrupted by further restarts). All REA outputs captured before the drop
  are recorded above; analysis continues with local tooling.

## Engineering issue observed on device (previous iteration)

A re-signed rebuild of the Android APK (signer `CN=CRD Client Dev`,
cert SHA-256 `5a82145c...`) installs and launches on an OnePlus 7 (Android 10,
arm64) but is rejected by Google Play services at first API access:
`SecurityException: PackageVerificationRslt: not allowed ... sha256=[5a82145c...]`.
Google's trust check binds the package to Google's signing key, so re-signed
copies of Google apps cannot use account/auth services. This is why the rebuild
targets are Windows/desktop (C/C++ receiver, Rust host) rather than a repackaged
APK.
