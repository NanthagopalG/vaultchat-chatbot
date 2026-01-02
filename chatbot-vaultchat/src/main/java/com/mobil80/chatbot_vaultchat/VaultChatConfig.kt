package com.mobil80.chatbot_vaultchat

import android.app.Application

data class VaultChatConfig(
    val application: Application,
    val apiKey: String,
    val primaryColor: String,
    val theme: VaultChat.Theme,
    val buttonContent: String = "💬",
    val buttonType: ButtonType = ButtonType.TEXT,
    val buttonShape: ButtonShape = ButtonShape.CIRCLE
)

enum class ButtonType {
    TEXT, IMAGE
}

enum class ButtonShape {
    CIRCLE, SQUARE
}
