# receiver-cpp

C/C++ rebuild of the **Chrome Remote Desktop receiver (client)**, derived from
the reference Android APK (79.0.3945.26) used strictly as protocol evidence.

Target platform: Windows/desktop (MinGW-w64). No Android NDK, ever.

## Status — v0.1 (CLI skeleton)

Builds and runs; prints reference linkage and module roadmap.

## Build

```powershell
# from open-crd\receiver-cpp
cmake -S . -B build -G "MinGW Makefiles"
cmake --build build
build\crd-receiver.exe status
```

(MinGW bin directory must be on PATH, e.g.
`C:\Users\raj\tools\mingw64\mingw64\bin`.)

## Roadmap

| Module | Purpose |
|---|---|
| `config` | local peer/host entries and settings |
| `auth` | OAuth token acquisition for the chromoting scope |
| `directory` | host list via the directory service |
| `ftl` | FTL signaling (`/chromoting_ftl_`) |
| `session` | WebRTC transport to the host |
