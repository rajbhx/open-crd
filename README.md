# open-crd

Open-source, clean-room C/C++ rebuild of the two shipped Chrome Remote Desktop
artifacts supplied by the operator:

| Side | Reference artifact | Version |
|---|---|---|
| Receiver (client) | `com.google.chromeremotedesktop_79.0.3945.26...apk` | 79.0.3945.26 |
| Host (Windows) | `chromeremotedesktophost.msi` | 154.0.8037.40 |

The rebuild is driven by REA evidence extracted from those artifacts
(see `docs/EVIDENCE.md` and `docs/PROTOCOL.md`).

No Google binaries are redistributed in this repository. No Android NDK is used
anywhere: the receiver target is a Windows/desktop C++ client; the APK is used
strictly as protocol evidence.

## Status — v0.1 (scaffold)

- [x] Open-source charter + MIT license
- [x] Reference artifact inventory + REA evidence index
- [ ] Protocol dossier (host registration, FTL signaling, session transport)
- [ ] Host: C/C++ implementation (registration, signaling, session)
- [ ] Receiver: C/C++ implementation (auth, host list, session view)
- [ ] Reproducible builds (CMake + MinGW)

## Layout (planned)

```
include/       public headers
src/common/    logging, config, platform utilities
src/directory/ directory service (host list, registration, heartbeats)
src/auth/      OAuth device/host token flows
src/ftl/       FTL signaling (websocket + protobuf framing)
src/session/   session transport plumbing
src/host/      Windows host entrypoint
src/receiver/  receiver (client) entrypoint
docs/          protocol dossier + evidence index
```

## Build

```powershell
cmake -S . -B build -G "MinGW Makefiles"
cmake --build build
```

## Legal

MIT-licensed original code, not affiliated with or endorsed by Google.
Protocol facts were derived from operator-supplied binaries for interoperability
and personal/experimental use. Connect only to accounts and machines you own.
