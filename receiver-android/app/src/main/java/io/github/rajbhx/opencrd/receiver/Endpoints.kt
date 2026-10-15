package io.github.rajbhx.opencrd.receiver

/**
 * Public remoting service endpoints used by Chrome Remote Desktop,
 * as extracted from the shipped artifacts (see docs/PROTOCOL.md).
 *
 * These are used for signaling and directory lookups. Media (video/audio/
 * input) flows over WebRTC between the client and the host after signaling.
 */
object Endpoints {
    /** FTL signaling (XMPP-over-TLS) — session offers/answers, ICE exchange. */
    const val FTL_SIGNALING = "instantmessaging-pa.googleapis.com"

    /** Directory/hostlist API — which computers are registered to an account. */
    const val DIRECTORY_API = "remotedesktop-pa.googleapis.com"

    /** Remoting cloud services (certificates/config, auxiliary services). */
    const val CLOUD_SERVICES = "remotingcloud-pa.googleapis.com"

    const val HTTPS_PORT = 443

    val all: List<String> = listOf(FTL_SIGNALING, DIRECTORY_API, CLOUD_SERVICES)
}
