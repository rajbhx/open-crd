package io.github.rajbhx.opencrd.receiver

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class SshConnectActivity : Activity() {

    private lateinit var hostField: EditText
    private lateinit var portField: EditText
    private lateinit var userField: EditText
    private lateinit var passField: EditText
    private lateinit var keyToggle: CheckBox
    private lateinit var keyField: EditText
    private lateinit var keyPassField: EditText
    private lateinit var statusText: TextView

    private val prefs by lazy { getSharedPreferences("opencrd", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
        }

        content.addView(TextView(this).apply {
            text = "SSH sign-in"
            textSize = 24f
            setTypeface(typeface, Typeface.BOLD)
        })
        content.addView(TextView(this).apply {
            text = "Connect to any computer that runs an SSH server: Windows " +
                "(OpenSSH Server feature), Linux, macOS, NAS. You get an " +
                "interactive command shell."
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(0, pad / 2, 0, pad)
        })

        hostField = field("Host (IP or name, e.g. 192.168.1.10)", InputType.TYPE_CLASS_TEXT)
        content.addView(hostField)
        portField = field("Port", InputType.TYPE_CLASS_NUMBER)
        content.addView(portField)
        userField = field("Username", InputType.TYPE_CLASS_TEXT)
        content.addView(userField)
        passField = field("Password", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        content.addView(passField)

        keyToggle = CheckBox(this).apply {
            text = "Use a private key instead of a password"
        }
        content.addView(keyToggle)

        keyField = field(
            "Private key (paste OpenSSH/PEM text)",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
        ).apply {
            minLines = 3
            gravity = Gravity.TOP
        }
        content.addView(keyField)
        keyPassField = field(
            "Key passphrase (if any)",
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
        )
        content.addView(keyPassField)

        keyToggle.setOnCheckedChangeListener { _, checked ->
            passField.isEnabled = !checked
            keyField.isEnabled = checked
            keyPassField.isEnabled = checked
        }
        keyField.isEnabled = false
        keyPassField.isEnabled = false

        content.addView(Button(this).apply {
            text = "Connect"
            isAllCaps = false
            setOnClickListener { connect() }
        })
        content.addView(Button(this).apply {
            text = "Forget saved host key for this address"
            isAllCaps = false
            setOnClickListener { forgetHostKey() }
        })

        statusText = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.parseColor("#B00020"))
            setPadding(0, pad / 2, 0, 0)
        }
        content.addView(statusText)

        val scroll = ScrollView(this).apply {
            addView(
                content,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                ),
            )
        }
        setContentView(scroll)

        hostField.setText(prefs.getString("last_host", "") ?: "")
        portField.setText(prefs.getString("last_port", "22") ?: "22")
        userField.setText(prefs.getString("last_user", "") ?: "")
    }

    private fun field(hintText: String, inputType: Int): EditText =
        EditText(this).apply {
            hint = hintText
            this.inputType = inputType
        }

    private fun connect() {
        val host = hostField.text.toString().trim()
        val port = portField.text.toString().trim().ifEmpty { "22" }.toIntOrNull()
        val user = userField.text.toString().trim()
        val useKey = keyToggle.isChecked
        val password = passField.text.toString()
        val keyText = keyField.text.toString()
        val keyPass = keyPassField.text.toString()

        if (host.isEmpty()) { statusText.text = "Enter a host address."; return }
        if (port == null || port !in 1..65535) { statusText.text = "Port must be 1-65535."; return }
        if (user.isEmpty()) { statusText.text = "Enter a username."; return }
        if (!useKey && password.isEmpty()) {
            statusText.text = "Enter a password (or switch to a private key)."
            return
        }
        if (useKey && keyText.isBlank()) {
            statusText.text = "Paste the private key text (or switch to a password)."
            return
        }

        prefs.edit()
            .putString("last_host", host)
            .putString("last_port", port.toString())
            .putString("last_user", user)
            .apply()

        statusText.text = ""
        val i = Intent(this, SshSessionActivity::class.java).apply {
            putExtra("host", host)
            putExtra("port", port)
            putExtra("user", user)
            putExtra("useKey", useKey)
            if (useKey) {
                putExtra("key", keyText)
                putExtra("keyPass", keyPass)
            } else {
                putExtra("password", password)
            }
        }
        startActivity(i)
    }

    private fun forgetHostKey() {
        val host = hostField.text.toString().trim()
        if (host.isEmpty()) {
            Toast.makeText(this, "Enter the host address first.", Toast.LENGTH_SHORT).show()
            return
        }
        val port = portField.text.toString().trim().ifEmpty { "22" }
        val edit = prefs.edit()
        edit.remove("hk_$host")
        edit.remove("hk_$host:$port")
        edit.apply()
        Toast.makeText(this, "Cleared saved host key for $host:$port", Toast.LENGTH_SHORT).show()
    }
}
