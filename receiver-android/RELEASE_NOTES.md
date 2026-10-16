# OpenCRD Receiver v0.2.0

**SSH sign-in arrives**: connect the receiver to any computer that runs an SSH
server, straight from the app.

## New in this build

- **Sign in via SSH** — host, port, username, password **or** a pasted private
  key (RSA/ECDSA, OpenSSH/PEM text).
  - interactive shell: type commands and see the output (ANSI-stripped
    terminal view — fine for `ipconfig`, `dir`, `systeminfo`, `git`, ...)
  - host keys are pinned on first use (trust-on-first-use): a changed host key
    is refused until you tap “Forget saved host key”, so a reinstalled server
    is a one-tap fix and a swapped key is a red flag.
  - the app never stores your password; host/port/username are remembered as
    defaults only.
- Works with Windows OpenSSH Server, Linux, macOS, NAS. On Windows: enable the
  “OpenSSH Server” optional feature and start the `sshd` service.
- Still zero native code: the SSH client is the pure-Java JSch library
  (BSD-style license) — no NDK, no Google Play services, runs on every ABI.
- Updating from 0.1.0 installs in place (same signing key).

## Roadmap

| Version | Milestone |
|---|---|
| 0.3 | Google sign-in (OAuth device flow) + host directory listing |
| 0.4 | FTL signaling channel (protobuf over TLS) |
| 0.5 | WebRTC session: video + keyboard/mouse input |
| 1.0 | day-to-day usable receiver |

## Notes

- Not affiliated with or endorsed by Google. Connect only to machines you own.
- `SHA256SUMS.txt` attached for integrity checking.
