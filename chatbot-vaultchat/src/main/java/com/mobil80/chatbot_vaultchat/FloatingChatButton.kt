package com.mobil80.chatbot_vaultchat

import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.toColorInt
import com.google.android.material.card.MaterialCardView
import java.net.URL
import java.util.concurrent.Executors

object FloatingChatButton {

    private var buttonView: View? = null
    private val executor = Executors.newSingleThreadExecutor()

    private fun getContrastColor(color: Int): Int {
        val luminance = ColorUtils.calculateLuminance(color)
        return if (luminance > 0.5) Color.BLACK else Color.WHITE
    }

    fun attach(context: Context) {
        val activity = context as? Activity ?: return
        if (buttonView != null) return

        val config = VaultChat.config
        val primaryColorInt = try { config.primaryColor.toColorInt() } catch (e: Exception) { Color.parseColor("#2563eb") }
        val contrastColor = getContrastColor(primaryColorInt)

        val card = MaterialCardView(context).apply {
            cardElevation = 12f
            setCardBackgroundColor(primaryColorInt)
            
            // Handle Shape
            radius = if (config.buttonShape == ButtonShape.CIRCLE) {
                if (config.buttonType == ButtonType.TEXT) 60f else 1000f
            } else {
                24f
            }

            setOnClickListener {
                ChatDialog.show(context)
            }
        }

        if (config.buttonType == ButtonType.IMAGE && config.buttonContent.startsWith("http")) {
            val container = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(140, 140)
            }
            val imageView = ImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageResource(android.R.drawable.ic_dialog_email)
                setColorFilter(contrastColor)
            }
            container.addView(imageView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            card.addView(container)

            executor.execute {
                try {
                    val url = URL(config.buttonContent)
                    val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                    activity.runOnUiThread { 
                        imageView.clearColorFilter()
                        imageView.setImageBitmap(bitmap) 
                    }
                } catch (e: Exception) {
                    // Fail gracefully
                }
            }
        } else {
            // Text mode - Dynamic width based on content
            val textView = TextView(context).apply {
                text = if (config.buttonContent.isEmpty()) "💬" else config.buttonContent
                textSize = 18f
                setTextColor(contrastColor)
                gravity = Gravity.CENTER
                typeface = Typeface.DEFAULT_BOLD
                setPadding(40, 25, 40, 25) // Good padding for text
            }
            card.addView(textView)
        }

        val params = FrameLayout.LayoutParams(
            WRAP_CONTENT,
            if (config.buttonType == ButtonType.IMAGE) 140 else WRAP_CONTENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            setMargins(0, 0, 64, 64)
            if (config.buttonType == ButtonType.IMAGE) {
                width = 140
            }
        }

        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        rootView.addView(card, params)
        buttonView = card
    }

    fun detach() {
        buttonView?.let {
            val parent = it.parent as? ViewGroup
            parent?.removeView(it)
        }
        buttonView = null
    }
}
