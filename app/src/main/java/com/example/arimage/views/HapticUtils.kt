package com.example.arimage.views

import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View

object HapticUtils {

    fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        if (view.isEnabled && event.action == MotionEvent.ACTION_DOWN) {
            performHapticFeedbackIfEnabled(view)
        }

        return false
    }

    private fun performHapticFeedbackIfEnabled(view: View) {
        if (view.isHapticFeedbackEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        }
    }
}