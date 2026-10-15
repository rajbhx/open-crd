# host-rs

Rust rebuild of the **Chrome Remote Desktop host (Windows)**, derived from the
reference MSI (`chromeremotedesktophost.msi`, 154.0.8037.40) used as evidence.

## Status — v0.1 (CLI skeleton)

Builds and runs (zero dependencies); prints reference linkage and module roadmap.

## Build

```powershell
# from open-crd\host-rs
cargo build
target\debug\crd-host.exe status
```

## Roadmap

| Module | Purpose |
|---|---|
| `config` | host configuration (host id/owner, credentials) |
| `auth` | OAuth token acquisition/refresh for the host scope |
| `directory` | registration, heartbeats, host lifecycle |
| `ftl` | FTL signaling strategy |
| `session` | WebRTC session (capture, encode, input) |
