package io.github.rajbhx.opencrd.receiver

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var statusLine: TextView
    private lateinit var logText: TextView
    private lateinit var logScroll: ScrollView
    private val logBuffer = StringBuilder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (16 * resources.displayMetrics.density).toInt()

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        content.addView(TextView(this).apply {
            text = "OpenCRD Receiver"
            textSize = 26f
            setTypeface(typeface, Typeface.BOLD)
        })

        statusLine = TextView(this).apply {
            text = "v" + BuildConfig.VERSION_NAME + "  •  no Google Play services  •  no NDK"
            textSize = 13f
            setTextColor(Color.GRAY)
        }
        content.addView(statusLine)

        content.addView(header("Purpose", pad))
        content.addView(
            body(
                "Open-source Chrome Remote Desktop receiver. This build is the " +
                    "foundation milestone: it verifies that this device can reach the " +
                    "public remoting endpoints that a real session will use.",
                pad,
            ),
        )

        content.addView(header("Network reachability self-test", pad))
        content.addView(
            body(
                "Runs DNS + TCP + TLS handshake checks against the signaling and " +
                    "directory endpoints. No account needed.",
                pad,
            ),
        )
        content.addView(Button(this).apply {
            text = "Run self-test"
            isAllCaps = false
            setOnClickListener { runSelfTest() }
        })

        content.addView(header("Roadmap", pad))
        content.addView(
            body(
                "0.1  foundation APK + endpoint self-test   (this build)\n" +
                    "0.2  OAuth device flow + host directory listing\n" +
                    "0.3  FTL signaling channel (protobuf over TLS)\n" +
                    "0.4  WebRTC session: video + keyboard/mouse\n" +
                    "1.0  day-to-day usable receiver",
                pad,
            ),
        )

        val contentScroll = ScrollView(this).apply {
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }

        logText = TextView(this).apply {
            textSize = 11f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#7FD1FF"))
            setPadding(pad, pad, pad, pad)
        }
        logScroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0D1B2A"))
            addView(
                logText,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }

        val logLabel = TextView(this).apply {
            text = "Log"
            textSize = 13f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(pad, pad, pad, pad / 2)
        }

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        rootLayout.addView(
            contentScroll,
            LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f),
        )
        rootLayout.addView(logLabel)
        rootLayout.addView(
            logScroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (170 * resources.displayMetrics.density).toInt(),
            ),
        )

        setContentView(rootLayout)

        appendLog(
            "ready • " + Build.MANUFACTURER + " " + Build.MODEL +
                " • Android " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")" +
                " • ABI " + Build.SUPPORTED_ABIS.joinToString(", "),
        )
        appendLog("endpoints: " + Endpoints.all.joinToString(", "))
    }

    private fun header(title: String, pad: Int): TextView =
        TextView(this).apply {
            text = title
            textSize = 17f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, pad, 0, pad / 3)
        }

    private fun body(value: String, pad: Int): TextView =
        TextView(this).apply {
            text = value
            textSize = 14f
            setPadding(0, 0, 0, pad / 2)
        }

    private fun appendLog(line: String) {
        val stamp = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        logBuffer.append('[').append(stamp).append("] ").append(line).append('\n')
        logText.text = logBuffer.toString()
        logScroll.post { logScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun runSelfTest() {
        appendLog("self-test: probing ${Endpoints.all.size} endpoints over TLS…")
        statusLine.text = "self-test running…"
        Thread {
            val results = NetProbe.probeAll(Endpoints.all)
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                var ok = 0
                for (r in results) {
                    if (r.ok) ok++
                    val tag = if (r.ok) "[ OK ]" else "[FAIL]"
                    appendLog("$tag ${r.host} — ${r.elapsedMs} ms — ${r.detail}")
                }
                statusLine.text =
                    "v" + BuildConfig.VERSION_NAME + "  •  self-test: $ok/${results.size} reachable"
            }
        }.start()
    }
}
