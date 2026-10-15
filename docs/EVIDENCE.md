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

## Transport evidence (host engine string sweep)

Method: the 44,891-entry unique-string sweep of `remoting_core.dll`
(`strings_remoting_core.txt`, ≥ 8 printable chars) was interrogated with
case-insensitive `Select-String` counts for protocol markers. Findings and the
full table are recorded in `docs/PROTOCOL.md` §4.1.

Key result: **no SSH** (`SSH-2.0`/`libssh`/`openssh`/`sshd`/`putty` all 0;
the single real `ssh` identifier lives in the remote-security-key module) and a
strong WebRTC stack presence (WebRTC/DTLS/SRTP/SCTP/ICE/STUN/TURN + FTL
signaling), consistent with Chrome Remote Desktop's published architecture.

## Ghidra-level analysis of the host engine (REA / Ghidra 12.1.4)

Environment and tuning (read-only "Windows P0" boundary):

- Ghidra 12.1.4 PUBLIC + Eclipse Temurin JDK 21; scoped `rea doctor` checks all
  healthy (headless mode, `win_x86_64` decompiler, native process authority
  verified).
- `REA_GHIDRA_STARTUP_TIMEOUT_MS=3600000` — the stock 330 s provider deadline
  is far below the engine's import+analysis cost on this 4 GB host. A
  default-profile cold-import attempt on `remoting_core.dll` hit
  `provider_timeout` at exactly 30:00 (operation `list_documents`), confirming
  the need for a longer deadline and a trimmed analyzer set.
- `REA_GHIDRA_DISABLED_ANALYZERS="Decompiler Parameter ID,Decompiler Switch
  Analysis,WindowsResourceReference"` — decompiler-heavy passes that do not
  contribute string/symbol/import/transport evidence.
- The deep engine analysis runs in a detached driver process (own REA MCP
  server + Ghidra session), so editor restarts observed during this session
  cannot interrupt the import; results are appended below as they land.

`remoting_host.exe` launcher (74,392 bytes, sha256
`1385d0d97cadff8c7cbffe25f010e503b028ac0e2952b634cd75fd860b796d7e`):

- Imports: `REMOTING_CORE.DLL::remoting::HostMain` plus 10 KERNEL32 stubs — the
  shipped host executable is a loader for the engine DLL.
- `search_strings "ssh"` → 0 matches; `search_procedures "ssh"` → 0 matches.
  (No PDB ships with the binaries; name-level absence complements a slower
  tooling caveat, see: lighter analysis profiles should treat "no matching
  symbol" as weaker than the string-sweep evidence in `docs/PROTOCOL.md` §4.1.)
- The engine reports Chromium branch `8037`
  (`…u_efs/branch-heads/8037@{#1026}_0040f9ac+0x54`), matching MSI product
  version 154.0.8037.40.
- Evidence IDs (session ledger): overview
  `ev_4638e329dc477bf302c128ece65683c8756b02fb35d5434c1cd832fc874b6ca9`;
  names/imports
  `ev_5c8847126c82c25351265d054af1713273949b052a200f178435bc4989e0810b`;
  procedures
  `ev_483a8688ef3ec2e19938b0d29caeffa8cc48a2463b95bbca8a56e62d522e93b3`;
  `ssh` string/procedure searches also in the ledger (`ev_b61a1617…`,
  `ev_d2a0592a…`).
