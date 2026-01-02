package com.mobil80.chatbot_vaultchat

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import androidx.core.graphics.ColorUtils
import androidx.core.widget.NestedScrollView
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ChatView(context: Context) : LinearLayout(context) {

    private val messagesContainer = LinearLayout(context)
    private val scrollView = NestedScrollView(context)
    private val inputField = EditText(context)
    private var typingIndicator: View? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    
    var onDismissListener: (() -> Unit)? = null

    private val primaryColor = try {
        Color.parseColor(VaultChat.config.primaryColor)
    } catch (e: Exception) {
        Color.parseColor("#2563eb")
    }

    private fun getContrastColor(color: Int): Int {
        val luminance = ColorUtils.calculateLuminance(color)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    private val onPrimaryTextColor = getContrastColor(primaryColor)

    private val isDarkMode: Boolean
        get() = when (VaultChat.config.theme) {
            VaultChat.Theme.DARK -> true
            VaultChat.Theme.LIGHT -> false
            VaultChat.Theme.AUTO -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        }

    private val backgroundColor: Int get() = if (isDarkMode) Color.parseColor("#111827") else Color.parseColor("#F9FAFB")
    private val surfaceColor: Int get() = if (isDarkMode) Color.parseColor("#1F2937") else Color.WHITE
    private val textColor: Int get() = if (isDarkMode) Color.WHITE else Color.BLACK
    private val botBubbleColor: Int get() = if (isDarkMode) Color.parseColor("#374151") else Color.parseColor("#E5E7EB")

    init {
        orientation = VERTICAL
        setBackgroundColor(backgroundColor)
        setupUI()
        
        addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                scrollView.postDelayed({
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }, 100)
            }
        }
    }

    private fun setupUI() {
        // 1. Header Bar
        val header = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(primaryColor)
            setPadding(40, 60, 40, 60)
            elevation = 10f
        }

        val textContainer = LinearLayout(context).apply {
            orientation = VERTICAL
            layoutParams = LayoutParams(0, WRAP_CONTENT, 1f)
            gravity = Gravity.CENTER_VERTICAL
        }

        // Clean unique header: Only "Powered by VaultChat"
        val poweredBy = TextView(context).apply {
            text = "Powered by VaultChat"
            setTextColor(onPrimaryTextColor)
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
        }

        textContainer.addView(poweredBy)
        header.addView(textContainer)

        val closeButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            setColorFilter(onPrimaryTextColor)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(20, 20, 20, 20)
            setOnClickListener {
                onDismissListener?.invoke()
            }
        }
        header.addView(closeButton)

        addView(header, LayoutParams(MATCH_PARENT, WRAP_CONTENT))

        // 2. Messages List
        messagesContainer.apply {
            orientation = VERTICAL
            setPadding(30, 30, 30, 30)
        }

        scrollView.apply {
            isFillViewport = true
            addView(messagesContainer)
        }

        val scrollParams = LayoutParams(MATCH_PARENT, 0, 1f)
        addView(scrollView, scrollParams)

        // 3. Input Area
        val inputBar = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(surfaceColor)
            setPadding(30, 25, 30, 25)
            elevation = 20f
        }

        inputField.apply {
            hint = "Type a message..."
            setHintTextColor(Color.GRAY)
            setTextColor(textColor)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(20, 20, 20, 20)
            textSize = 16f
        }

        val sendButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.ic_menu_send)
            setColorFilter(primaryColor)
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(20, 20, 20, 20)
            setOnClickListener { sendMessage() }
        }

        val inputParams = LayoutParams(0, WRAP_CONTENT, 1f)
        inputBar.addView(inputField, inputParams)
        inputBar.addView(sendButton)

        addView(inputBar, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
    }

    private fun showTypingIndicator() {
        if (typingIndicator != null) return
        val wrapper = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.START
            setPadding(0, 15, 0, 15)
        }
        val bubble = LinearLayout(context).apply {
            setPadding(50, 25, 50, 25)
            val shape = GradientDrawable().apply {
                cornerRadius = 40f
                setColor(botBubbleColor)
            }
            background = shape
            val progressBar = ProgressBar(context, null, android.R.attr.progressBarStyleSmall).apply {
                indeterminateTintList = ColorStateList.valueOf(Color.GRAY)
            }
            addView(progressBar)
        }
        wrapper.addView(bubble)
        messagesContainer.addView(wrapper)
        typingIndicator = wrapper
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun hideTypingIndicator() {
        typingIndicator?.let {
            messagesContainer.removeView(it)
            typingIndicator = null
        }
    }

    private fun addMessage(sender: String, text: String, isUser: Boolean) {
        val wrapper = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = if (isUser) Gravity.END else Gravity.START
            setPadding(0, 15, 0, 15)
        }

        val bubble = TextView(context).apply {
            this.text = text
            textSize = 15f
            setPadding(40, 25, 40, 25)
            maxWidth = (context.resources.displayMetrics.widthPixels * 0.75).toInt()
            val shape = GradientDrawable().apply {
                cornerRadius = 40f
                if (isUser) {
                    setColor(primaryColor)
                    setTextColor(onPrimaryTextColor)
                } else {
                    setColor(botBubbleColor)
                    setTextColor(textColor)
                }
            }
            background = shape
        }

        wrapper.addView(bubble)
        messagesContainer.addView(wrapper)
        scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun sendMessage() {
        val message = inputField.text.toString().trim()
        if (message.isEmpty()) return

        val apiKey = VaultChat.config.apiKey
        if (apiKey.isBlank() || apiKey == "YOUR_API_KEY") {
            addMessage("System", "Error: API Key is not configured.", false)
            return
        }

        addMessage("You", message, true)
        inputField.text.clear()
        showTypingIndicator()

        executor.execute {
            try {
                val response = callVaultChatApi(apiKey, message)
                handler.post {
                    hideTypingIndicator()
                    addMessage("Bot", response, false)
                }
            } catch (e: Exception) {
                handler.post {
                    hideTypingIndicator()
                    addMessage("System", "Error: ${e.message}", false)
                }
            }
        }
    }

    private fun callVaultChatApi(apiKey: String, question: String): String {
        val url = URL("https://api.vaultchat.io/askChatbot")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true

        val jsonBody = JSONObject()
        jsonBody.put("command", "askChatbot")
        jsonBody.put("api_key", apiKey)
        jsonBody.put("question", question)

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

        return if (conn.responseCode == 200) {
            val responseString = conn.inputStream.bufferedReader().use { it.readText() }
            try {
                val jsonResponse = JSONObject(responseString)
                val dataObj = jsonResponse.optJSONObject("data")
                val blocksArray = dataObj?.optJSONArray("blocks")
                
                if (blocksArray != null) {
                    val result = StringBuilder()
                    for (i in 0 until blocksArray.length()) {
                        val block = blocksArray.optJSONObject(i)
                        val text = block?.optString("text")
                        if (text != null) {
                            if (result.isNotEmpty()) result.append("\n")
                            result.append(text)
                        }
                    }
                    if (result.isNotEmpty()) result.toString() else "No response"
                } else {
                    jsonResponse.optString("answer", responseString)
                }
            } catch (e: Exception) {
                responseString
            }
        } else {
            "Error: Server returned code ${conn.responseCode}"
        }
    }
}
