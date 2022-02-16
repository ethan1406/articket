package com.example.arimage.views

import android.graphics.PorterDuff

object TintUtils {

    /**
     * Convert an attribute value to the corresponding [PorterDuff.Mode].
     */
    fun parseTintMode(value: Int, defaultMode: PorterDuff.Mode): PorterDuff.Mode {
        return when (value) {
            3 -> PorterDuff.Mode.SRC_OVER
            4, 6, 7, 8, 10, 11, 12, 13 -> defaultMode
            5 -> PorterDuff.Mode.SRC_IN
            9 -> PorterDuff.Mode.SRC_ATOP
            14 -> PorterDuff.Mode.MULTIPLY
            15 -> PorterDuff.Mode.SCREEN
            16 -> PorterDuff.Mode.ADD
            else                       -> defaultMode
        }
    }
}