package com.mobil80.chatbot_vaultchat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mobil80.vaultchat_chatbot.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContentView(R.layout.activity_main)

        // Initialize VaultChat with  simple string props
        VaultChat.init(
            context = this,
            apiKey = "YOUR_API_KEY",
            primaryColor = "red",
            theme = "light",
            buttonContent = "",
            buttonType = "text",
            buttonShape = "square"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        VaultChat.destroy()
    }
}
