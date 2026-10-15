# Protocol dossier (living document)

Facts below are extracted from the reference artifacts — REA evidence and direct
string/PE analysis of `remoting_core.dll` (host 154.0.8037.40, 33,905,816 bytes,
sha256 `a0065892...`). Items marked **(observed)** are literal strings extracted
from the binary; items marked ⏳ are pending extraction.

The embedded source paths (`..\..\remoting\...`) show the host is built from the
Chromium remoting codebase; they are cited as observed evidence.

## 0. Session model

receiver (client) ⇄ FTL signaling ⇄ session transport (WebRTC) ⇄ host

- Host-side IPC **(observed)**:
  `remoting.mojom.ChromotingHostServices`, `remoting.mojom.ChromotingSessionServices`
  over `chromoting.host_services_mojo_ipc`
  (`remoting/host/chromoting_host_services_client.cc`, `..._server.cc`)
- ⏳ full message-flow trace

## 1. Authentication & credentials

- Host OAuth scope **(observed)**: `https://www.googleapis.com/auth/chromoting.me2me.host`
- Token machinery **(observed)**: `remoting/base/oauth_token_getter_impl.cc`,
  `oauth_token_getter_proxy.cc`, `passthrough_oauth_token_getter.cc`
- Flows **(observed)**: "Fetching OAuth token from Auth Code",
  "Refreshing OAuth Access token", `GaiaOAuthClient`, endpoint
  `oauthaccountmanager.googleapis.com`
- Errors **(observed)**: `INVALID_OAUTH_CREDENTIALS`
- ⏳ receiver-side client flow (from APK evidence)

## 2. Directory / cloud endpoints

- **(observed)** `remotedesktop-pa.googleapis.com`, `remotingcloud.googleapis.com`,
  `remotingcloud-pa.googleapis.com`, `remotedesktop.corp.googleapis.com`
- Host identity **(observed)**: `CN=chromoting`, computer-name template `chromoting-%ld-%d`
- ⏳ request/response shapes (registration, host list, heartbeat)

## 3. FTL signaling

- FTL stream path **(observed)**: `/chromoting_ftl_` (`remoting/signaling/signaling_id_util.cc`)
- Strategy **(observed)**: `remoting/signaling/ftl_signal_strategy.cc`
- Messaging endpoint **(observed)**: `instantmessaging-pa.googleapis.com`
- Tokens **(observed)**: `signalingAccessToken`
- States/errors **(observed)**: `SIGNALING_ERROR`, `SIGNALING_TIMEOUT`,
  "Signaling is not disconnected. State: ..."

## 4. Session transport

- WebRTC-based **(observed)**: `third_party/webrtc/...` paths;
  `remoting/codec/webrtc_video_encoder_av1.cc`, `webrtc_video_encoder_vpx.cc`
- ICE/STUN/TURN machinery **(observed)**: `third_party/webrtc/api/transport/stun.cc`
- ⏳ channel map (video/control/input/clipboard), codec negotiation

## 5. Helpers & browser integration

- MSI helper binaries: `remoting_native_messaging_host.exe`, `remote_webauthn.exe`,
  `remote_security_key.exe`, `remote_open_url.exe`, `remote_assistance_host*.exe`
  (see `docs/EVIDENCE.md`)
- Protobufs **(observed)**: `remoting.protocol.RemoteWebAuthn.*`,
  `remoting.protocol.SecurityKeyMessage`,
  `remoting.protocol.UrlForwarderControl.*`
- ⏳ native-messaging message contract

## Evidence sources

- String sweep of `remoting_core.dll`: 44,891 unique printable strings (≥ 8 chars) —
  working artifact `strings_remoting_core.txt` (outside repo)
- REA identity evidence: `docs/EVIDENCE.md`
