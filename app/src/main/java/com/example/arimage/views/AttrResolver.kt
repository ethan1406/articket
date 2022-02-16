package com.example.arimage.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources

/**
 * Shared utility methods used for resolving themed values.
 */
object AttrResolver {

    /**
     * Returns the color associated with the specified theme attribute.
     */
    @JvmStatic
    @ColorInt
    fun getColor(context: Context, @AttrRes attr: Int): Int {
        val array = context.obtainStyledAttributes(null, intArrayOf(attr))
        try {
            return array.getColor(0, 0)
        } finally {
            array.recycle()
        }
    }

    /**
     * Returns the [ColorStateList] associated with the specified theme attribute.
     */
    @JvmStatic
    @SuppressLint("AppCompatResourcesColorStateListInflation")
    fun getColorStateList(context: Context, @AttrRes attr: Int): ColorStateList? {
        val array = context.obtainStyledAttributes(null, intArrayOf(attr))
        try {
            val resourceId = array.getResourceId(0, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
            return array.getColorStateList(0)
        } finally {
            array.recycle()
        }
    }

    /**
     * Returns the [Drawable] associated with the specified theme attribute.
     */
    @JvmStatic
    @SuppressLint("AppCompatResourcesDrawableInflation")
    fun getDrawable(context: Context, @AttrRes attr: Int): Drawable? {
        val array = context.obtainStyledAttributes(null, intArrayOf(attr))
        try {
            val resourceId = array.getResourceId(0, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getDrawable(context, resourceId)
                if (value != null) {
                    return value
                }
            }
            return array.getDrawable(0)
        } finally {
            array.recycle()
        }
    }

    /*
     * Returns the dimension associated with the specified theme attribute.
     */
    @JvmStatic
    @Px
    fun getDimension(context: Context, @AttrRes attr: Int): Float {
        val array = context.obtainStyledAttributes(null, intArrayOf(attr))
        try {
            return array.getDimension(0, 0f)
        } finally {
            array.recycle()
        }
    }
}
