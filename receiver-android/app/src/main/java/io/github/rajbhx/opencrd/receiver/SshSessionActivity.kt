package io.github.rajbhx.opencrd.receiver

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.HostKey
import com.jcraft.jsch.JSchHostKeyException
import com.jcraft.jsch.HostKeyRepository
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.UIKeyboardInteractive
import com.jcraft.jsch.UserInfo
import java.io.InputStream
import java.io.OutputStream
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.MessageDigest
import java.util.Properties

class SshSessionActivity : Activity() {

    private lateinit var statusText: TextView
    private lateinit var output: TextView
    private lateinit var input: EditText
    private lateinit var scroll: ScrollView

    private val outputBuffer = StringBuilder()
    private var session: Session? = null
    private var channel: ChannelShell? = null

    @Volatile
    private var readerRunning = false
    private var targetLabel = ""

    private val prefs by lazy { getSharedPreferences("opencrd", MODE_PRIVATE) }

    private val ansi = Regex(
        "\\x1B\\[[0-9;?]*[ -/]*[@-~]" +
            "|\\x1B\\][^\\x07]*(?:\\x07|\\x1B\\\\)" +
            "|\\x1B[=>]",
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (12 * resources.displayMetrics.density).toInt()

        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        statusText = TextView(this).apply {
            text = "connecting…"
            textSize = 12f
            setPadding(pad, pad, pad, pad / 2)
            setBackgroundColor(Color.parseColor("#10243F"))
            setTextColor(Color.WHITE)
        }
        root.addView(statusText)

        output = TextView(this).apply {
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#7FD1FF"))
            setPadding(pad, pad, pad, pad)
        }
        scroll = ScrollView(this).apply {
            setBackgroundColor(Color.parseColor("#0D1B2A"))
            addView(
                output,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        root.addView(scroll, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))

        val inputRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(pad / 2, pad / 2, pad / 2, pad / 2)
        }
        input = EditText(this).apply {
            hint = "command"
            textSize = 13f
            typeface = Typeface.MONOSPACE
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            isSingleLine = true
            isEnabled = false
        }
        inputRow.addView(input, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        inputRow.addView(Button(this).apply {
            text = "Send"
            isAllCaps = false
            setOnClickListener { send() }
        })
        root.addView(inputRow)

        root.addView(Button(this).apply {
            text = "Disconnect"
            isAllCaps = false
            setOnClickListener { finish() }
        })

        setContentView(root)

        val host = intent.getStringExtra("host") ?: ""
        val port = intent.getIntExtra("port", 22)
        val user = intent.getStringExtra("user") ?: ""
        targetLabel = "$user@$host:$port"
        if (host.isEmpty() || user.isEmpty()) {
            appendLog("[error] missing host or username")
            return
        }

        appendLog("connecting to $targetLabel …")
        Thread { doConnect(host, port, user) }.start()
    }

    private fun doConnect(host: String, port: Int, user: String) {
        val useKey = intent.getBooleanExtra("useKey", false)
        val jsch = JSch()
        val secretForPrompts: String
        try {
            if (useKey) {
                val keyText = intent.getStringExtra("key") ?: ""
                val keyPass = intent.getStringExtra("keyPass") ?: ""
                secretForPrompts = keyPass
                jsch.addIdentity(
                    "pasted-key",
                    keyText.toByteArray(Charsets.UTF_8),
                    null,
                    if (keyPass.isEmpty()) null else keyPass.toByteArray(Charsets.UTF_8),
                )
            } else {
                secretForPrompts = intent.getStringExtra("password") ?: ""
            }
        } catch (e: JSchException) {
            post { appendLog("[error] could not load private key: ${e.message}") }
            return
        }

        try {
            val s = jsch.getSession(user, host, port)
            if (!useKey) s.setPassword(intent.getStringExtra("password") ?: "")
            s.setHostKeyRepository(TofuStore(host, port))
            val cfg = Properties().apply {
                put("StrictHostKeyChecking", "yes")
                put(
                    "PreferredAuthentications",
                    if (useKey) {
                        "publickey,keyboard-interactive,password"
                    } else {
                        "password,keyboard-interactive,publickey"
                    },
                )
            }
            s.setConfig(cfg)
            s.userInfo = object : UserInfo, UIKeyboardInteractive {
                override fun getPassphrase(): String = secretForPrompts
                override fun getPassword(): String = secretForPrompts
                override fun promptPassword(message: String?): Boolean = false
                override fun promptPassphrase(message: String?): Boolean = false
                override fun promptYesNo(message: String?): Boolean = false
                override fun showMessage(message: String?) {}
                override fun promptKeyboardInteractive(
                    destination: String?,
                    name: String?,
                    instruction: String?,
                    prompt: Array<String>?,
                    echo: BooleanArray?,
                ): Array<String> = Array(prompt?.size ?: 0) { secretForPrompts }
            }
            s.serverAliveInterval = 15000
            s.connect(20000)
            session = s
        } catch (e: Throwable) {
            post {
                appendLog("[error] " + friendly(e))
                statusText.text = "connection failed"
            }
            try { session?.disconnect() } catch (_: Throwable) {}
            return
        }

        val s = session ?: return
        try {
            val hk = s.hostKey
            val fp = try { hk.getFingerPrint(jsch) } catch (t: Throwable) { "(unavailable)" }
            post { appendLog("[host key] $fp (pinned on first use)") }

            val ch: ChannelShell = try {
                val c = s.openChannel("shell") as ChannelShell
                c.setPtyType("xterm-256color")
                c.connect(15000)
                c
            } catch (e: JSchException) {
                post { appendLog("[warn] PTY shell failed (${e.message}); retrying without PTY") }
                val c2 = s.openChannel("shell") as ChannelShell
                c2.setPty(false)
                c2.connect(15000)
                c2
            }
            channel = ch
            post {
                statusText.text = "connected — $targetLabel"
                input.isEnabled = true
                appendLog("connected. type a command below and press Send.")
            }
            readerRunning = true
            Thread { readLoop(ch.inputStream) }.start()
        } catch (e: Throwable) {
            post {
                appendLog("[error] " + friendly(e))
                statusText.text = "session failed"
            }
            try { s.disconnect() } catch (_: Throwable) {}
        }
    }

    private fun readLoop(stream: InputStream) {
        val buf = ByteArray(4096)
        try {
            while (readerRunning) {
                val n = stream.read(buf)
                if (n < 0) break
                if (n == 0) continue
                var chunk = String(buf, 0, n, Charsets.UTF_8)
                chunk = chunk.replace("\r\n", "\n").replace("\r", "")
                chunk = ansi.replace(chunk, "")
                appendChunk(chunk)
            }
        } catch (_: Throwable) {
        } finally {
            post {
                statusText.text = "disconnected"
                input.isEnabled = false
                appendChunk("\n[session closed]\n")
            }
        }
    }

    private fun send() {
        val ch = channel ?: return
        val cmd = input.text.toString()
        input.setText("")
        try {
            val os: OutputStream = ch.outputStream
            os.write((cmd + "\n").toByteArray(Charsets.UTF_8))
            os.flush()
            appendLog("$ $cmd")
        } catch (e: Throwable) {
            appendLog("[error] send failed: ${e.message}")
        }
    }

    private fun friendly(e: Throwable): String {
        val m = e.message ?: ""
        return when {
            e is JSchHostKeyException ->
                "saved host key changed! If the server was reinstalled or " +
                    "rekeyed, tap 'Forget saved host key' on the previous screen " +
                    "and connect again."
            m.contains("Auth fail", ignoreCase = true) ->
                "authentication failed — check username/password (or use a " +
                    "private key; some servers disable password auth)."
            e is UnknownHostException ->
                "host not found — check the address."
            e is SocketTimeoutException || e is ConnectException ->
                "could not reach $targetLabel — check the address, port and " +
                    "firewall (port 22 open?)."
            else -> "${e.javaClass.simpleName}: $m".trim()
        }
    }

    private fun post(block: () -> Unit) {
        runOnUiThread {
            if (!isFinishing && !isDestroyed) block()
        }
    }

    private fun appendLog(line: String) = appendChunk("$line\n")

    private fun appendChunk(text: String) {
        if (text.isEmpty()) return
        post {
            outputBuffer.append(text)
            if (outputBuffer.length > 300_000) {
                outputBuffer.delete(0, 100_000)
                outputBuffer.insert(0, "[…older output trimmed…]\n")
            }
            output.text = outputBuffer.toString()
            scroll.post { scroll.fullScroll(View.FOCUS_DOWN) }
        }
    }

    private fun sha256Hex(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(data)
        val sb = StringBuilder(digest.size * 2)
        for (b in digest) sb.append(String.format("%02x", b))
        return sb.toString()
    }

    /**
     * Trust-on-first-use host key store: pins the first key seen for an
     * address and refuses a later change until the user clears it.
     */
    private inner class TofuStore(host: String, port: Int) : HostKeyRepository {
        private val keyId: String = if (port == 22) host else "$host:$port"

        override fun check(host: String, key: ByteArray): Int {
            val fp = sha256Hex(key)
            val stored = prefs.getString("hk_$keyId", null)
            if (stored == null) {
                prefs.edit().putString("hk_$keyId", fp).apply()
                return HostKeyRepository.OK
            }
            return if (stored == fp) HostKeyRepository.OK else HostKeyRepository.CHANGED
        }

        override fun add(hostkey: HostKey, ui: UserInfo?) {
            // Pinning is handled in check(); nothing to do here.
        }

        override fun remove(host: String, type: String) {
            prefs.edit().remove("hk_$keyId").apply()
        }

        override fun remove(host: String, type: String, key: ByteArray) {
            prefs.edit().remove("hk_$keyId").apply()
        }

        override fun getKnownHostsRepositoryID(): String = "opencrd-preferences"

        override fun getHostKey(): Array<HostKey> = emptyArray()

        override fun getHostKey(host: String, type: String): Array<HostKey> = emptyArray()
    }

    override fun onDestroy() {
        super.onDestroy()
        readerRunning = false
        try { channel?.disconnect() } catch (_: Throwable) {}
        try { session?.disconnect() } catch (_: Throwable) {}
    }
}
