package com.mobil80.chatbot_vaultchat

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

object ChatDialog {

    fun show(context: Context) {
        val dialog = BottomSheetDialog(context)
        val chatView = ChatView(context)
        
        // Handle the close button from ChatView
        chatView.onDismissListener = {
            dialog.dismiss()
        }
        
        dialog.setContentView(chatView)

        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            
            val layoutParams = it.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.layoutParams = layoutParams
        }

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        
        dialog.show()
    }
}
