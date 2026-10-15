package io.github.rajbhx.opencrd.receiver

import java.net.InetSocketAddress
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

/**
 * Network reachability probes: DNS + TCP + real TLS handshake against the
 * remoting endpoints. This validates that a future session has a viable path
 * to the signaling/directory infrastructure without needing an account.
 */
object NetProbe {

    data class ProbeResult(
        val host: String,
        val ok: Boolean,
        val elapsedMs: Long,
        val detail: String,
    )

    fun probe(host: String, port: Int = Endpoints.HTTPS_PORT, timeoutMs: Int = 8000): ProbeResult {
        val start = System.currentTimeMillis()
        var socket: SSLSocket? = null
        return try {
            val ssl = SSLSocketFactory.getDefault().createSocket() as SSLSocket
            socket = ssl
            ssl.connect(InetSocketAddress(host, port), timeoutMs)
            ssl.soTimeout = timeoutMs
            ssl.startHandshake()
            val peer = try {
                ssl.session.peerPrincipal?.name ?: "(no peer identity)"
            } catch (t: Throwable) {
                "(peer identity unavailable: ${t.javaClass.simpleName})"
            }
            ProbeResult(host, true, System.currentTimeMillis() - start, "TLS ok — $peer")
        } catch (t: Throwable) {
            ProbeResult(
                host,
                false,
                System.currentTimeMillis() - start,
                t.javaClass.simpleName + ": " + (t.message ?: ""),
            )
        } finally {
            try {
                socket?.close()
            } catch (_: Throwable) {
            }
        }
    }

    fun probeAll(hosts: List<String>): List<ProbeResult> = hosts.map { probe(it) }
}
