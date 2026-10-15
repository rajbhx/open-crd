# OpenCRD Receiver v0.1.0

Foundation build of the OpenCRD Receiver — a clean-room, open-source Chrome
Remote Desktop receiver for Android.

## What works today

- Installs and launches on Android 8.0+ (minSdk 26). Pure JVM bytecode:
  no native libraries, works on arm64, armv7, x86_64 and x86.
- **Network reachability self-test**: DNS + TCP + TLS handshake against the
  three public remoting endpoints
  (`instantmessaging-pa`, `remotedesktop-pa`, `remotingcloud-pa`.googleapis.com),
  including the server certificate identity.
- Diagnostics log: device model, Android version, ABIs, endpoint list.

## Roadmap

| Version | Milestone |
|---|---|
| 0.2 | OAuth device flow + host directory listing (no Play services) |
| 0.3 | FTL signaling channel (protobuf over TLS) |
| 0.4 | WebRTC session: video + keyboard/mouse input |
| 1.0 | day-to-day usable receiver |

## Notes

- No Google Play services, no NDK, no Google-authored binaries inside.
- Signed with the project release key; `SHA256SUMS.txt` is attached.
- Not affiliated with or endorsed by Google. Connect only to accounts and
  machines you own.
