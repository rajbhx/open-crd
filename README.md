# open-crd

[![ci](https://github.com/rajbhx/open-crd/actions/workflows/ci.yml/badge.svg)](https://github.com/rajbhx/open-crd/actions/workflows/ci.yml)

Open-source rebuild of the two shipped Chrome Remote Desktop artifacts supplied
by the operator, in the language the ecosystem demands for each side:

| Side | Language | Reference artifact | Version |
|---|---|---|---|
| Receiver (client) | C/C++ (`receiver-cpp/`) | `com.google.chromeremotedesktop_79.0.3945.26...apk` | 79.0.3945.26 |
| Host (Windows) | Rust (`host-rs/`) | `chromeremotedesktophost.msi` | 154.0.8037.40 |

The rebuild is driven by REA evidence extracted from those artifacts
(see `docs/EVIDENCE.md` and `docs/PROTOCOL.md`).

No Google binaries are redistributed in this repository. No Android NDK is used
anywhere: the receiver target is a Windows/desktop C++ client; the APK is used
strictly as protocol evidence.

## Status — v0.1 (scaffold)

- [x] Open-source charter + MIT license
- [x] Reference artifact inventory + REA evidence index
- [x] Receiver scaffold (C/C++, CMake) — builds in CI
- [x] Host scaffold (Rust, zero dependencies) — builds in CI
- [x] GitHub Actions: both projects build, smoke-run, and publish artifacts
- [ ] Protocol dossier (host registration, FTL signaling, session transport)
- [ ] Receiver: auth, host list, session view
- [ ] Host: registration, signaling, session

## Layout

```
receiver-cpp/  C/C++ receiver (client) — CMake project
host-rs/       Rust Windows host — Cargo project, zero dependencies
docs/          protocol dossier + evidence index
.github/       CI workflows
```

Planned modules per side (details in each subproject README):
`config`, `auth`, `directory`, `ftl` (signaling), `session` (transport).

## Builds

All builds run in CI (GitHub Actions) — see
[Actions](https://github.com/rajbhx/open-crd/actions). Published workflow
artifacts:

- `crd-receiver-windows-x64` — self-contained x64 executable (UCRT64 MinGW)
- `crd-host-windows-x64` — self-contained x64 executable (static CRT)

## Legal

MIT-licensed original code, not affiliated with or endorsed by Google.
Protocol facts were derived from operator-supplied binaries for interoperability
and personal/experimental use. Connect only to accounts and machines you own.
