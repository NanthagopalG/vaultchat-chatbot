package com.mobil80.chatbot_vaultchat

import android.app.Application
import android.content.Context

object VaultChat {

    internal lateinit var config: VaultChatConfig
        private set

    fun init(
        context: Context,
        apiKey: String,
        primaryColor: String = "#2563eb",
        theme: String = "auto",
        buttonContent: String = "💬",
        buttonType: String = "text",
        buttonShape: String = "circle"
    ) {
        val mappedTheme = when (theme.lowercase()) {
            "light" -> Theme.LIGHT
            "dark" -> Theme.DARK
            else -> Theme.AUTO
        }

        val mappedButtonType = when (buttonType.lowercase()) {
            "image" -> ButtonType.IMAGE
            else -> ButtonType.TEXT
        }

        val mappedButtonShape = when (buttonShape.lowercase()) {
            "square" -> ButtonShape.SQUARE
            else -> ButtonShape.CIRCLE
        }

        config = VaultChatConfig(
            application = context.applicationContext as Application,
            apiKey = apiKey,
            primaryColor = primaryColor,
            theme = mappedTheme,
            buttonContent = buttonContent,
            buttonType = mappedButtonType,
            buttonShape = mappedButtonShape
        )

        FloatingChatButton.attach(context)
    }

    fun destroy() {
        FloatingChatButton.detach()
    }

    enum class Theme {
        LIGHT, DARK, AUTO
    }
}
