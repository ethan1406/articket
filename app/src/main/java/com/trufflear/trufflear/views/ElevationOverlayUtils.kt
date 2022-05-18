package com.trufflear.trufflear.views

import android.view.View

object ElevationOverlayUtils {

    /**
     * Returns the absolute elevation of the parent of the provided `view`, or in other words,
     * the sum of the elevations of all ancestors of the `view`.
     */
    fun getParentAbsoluteElevation(view: View): Float {
        var absoluteElevation = 0f
        var viewParent = view.parent
        while (viewParent is View) {
            absoluteElevation += viewParent.elevation
            viewParent = viewParent.getParent()
        }
        return absoluteElevation
    }
}