package com.trufflear.trufflear.views

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import androidx.annotation.AnyRes
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources

/**
 * A class that wraps a [TypedArray] and provides the same public API
 * surface. The purpose of this class is so that we can intercept calls to new APIs.
 */
class TypedArrayCompat private constructor(
    private val context: Context,
    private val typedArray: TypedArray,
) {

    companion object {

        fun obtainStyledAttributes(
            context: Context,
            set: AttributeSet?,
            @StyleableRes attrs: IntArray,
        ): TypedArrayCompat {
            return TypedArrayCompat(context, context.obtainStyledAttributes(set, attrs))
        }

        fun obtainStyledAttributes(
            context: Context,
            set: AttributeSet?,
            @StyleableRes attrs: IntArray,
            @AttrRes defStyleAttr: Int,
            @StyleRes defStyleRes: Int,
        ): TypedArrayCompat {
            return TypedArrayCompat(
                context,
                context.obtainStyledAttributes(set, attrs, defStyleAttr, defStyleRes)
            )
        }
    }

    fun getIndexCount(): Int {
        return typedArray.indexCount
    }

    fun getResources(): Resources {
        return typedArray.resources
    }

    fun getPositionDescription(): String {
        return typedArray.positionDescription
    }

    fun getChangingConfigurations(): Int {
        return typedArray.changingConfigurations
    }

    @SuppressWarnings("AppCompatResourcesDrawableInflation")
    fun getDrawable(@StyleableRes index: Int): Drawable? {
        if (typedArray.hasValue(index)) {
            val resourceId = typedArray.getResourceId(index, 0)
            if (resourceId != 0) {
                return AppCompatResources.getDrawable(context, resourceId)
            }
        }
        return typedArray.getDrawable(index)
    }

    fun length(): Int {
        return typedArray.length()
    }

    fun getIndex(at: Int): Int {
        return typedArray.getIndex(at)
    }

    @SuppressWarnings("TypedArrayGetText")
    fun getText(@StyleableRes index: Int): CharSequence? {
        val stringResId = typedArray.getResourceId(index, 0)
        return if (stringResId != 0) {
            typedArray.resources.getText(stringResId)
        } else {
            typedArray.getText(index)
        }
    }

    @SuppressWarnings("TypedArrayGetText")
    fun getString(@StyleableRes index: Int): String? {
        val stringResId = typedArray.getResourceId(index, 0)
        return if (stringResId != 0) {
            typedArray.resources.getString(stringResId)
        } else {
            typedArray.getString(index)
        }
    }

    fun getNonResourceString(@StyleableRes index: Int): String {
        return typedArray.getNonResourceString(index)
    }

    fun getBoolean(@StyleableRes index: Int, defValue: Boolean): Boolean {
        return typedArray.getBoolean(index, defValue)
    }

    fun getInt(@StyleableRes index: Int, defValue: Int): Int {
        return typedArray.getInt(index, defValue)
    }

    fun getFloat(@StyleableRes index: Int, defValue: Float): Float {
        return typedArray.getFloat(index, defValue)
    }

    @ColorInt
    fun getColor(@StyleableRes index: Int, @ColorInt defValue: Int): Int {
        return typedArray.getColor(index, defValue)
    }

    @SuppressWarnings("AppCompatResourcesColorStateListInflation")
    fun getColorStateList(@StyleableRes index: Int): ColorStateList? {
        if (typedArray.hasValue(index)) {
            val resourceId = typedArray.getResourceId(index, 0)
            if (resourceId != 0) {
                val value = AppCompatResources.getColorStateList(context, resourceId)
                if (value != null) {
                    return value
                }
            }
        }
        return typedArray.getColorStateList(index)
    }

    fun getInteger(@StyleableRes index: Int, defValue: Int): Int {
        return typedArray.getInteger(index, defValue)
    }

    fun getDimension(@StyleableRes index: Int, defValue: Float): Float {
        return typedArray.getDimension(index, defValue)
    }

    fun getDimensionPixelOffset(@StyleableRes index: Int, defValue: Int): Int {
        return typedArray.getDimensionPixelOffset(index, defValue)
    }

    fun getDimensionPixelSize(@StyleableRes index: Int, defValue: Int): Int {
        return typedArray.getDimensionPixelSize(index, defValue)
    }

    fun getLayoutDimension(@StyleableRes index: Int, name: String): Int {
        return typedArray.getLayoutDimension(index, name)
    }

    fun getLayoutDimension(@StyleableRes index: Int, defValue: Int): Int {
        return typedArray.getLayoutDimension(index, defValue)
    }

    fun getFraction(@StyleableRes index: Int, base: Int, pbase: Int, defValue: Float): Float {
        return typedArray.getFraction(index, base, pbase, defValue)
    }

    @AnyRes
    fun getResourceId(@StyleableRes index: Int, defValue: Int): Int {
        return typedArray.getResourceId(index, defValue)
    }

    fun getTextArray(@StyleableRes index: Int): Array<CharSequence> {
        return typedArray.getTextArray(index)
    }

    fun getValue(@StyleableRes index: Int, outValue: TypedValue): Boolean {
        return typedArray.getValue(index, outValue)
    }

    fun getType(@StyleableRes index: Int): Int {
        return typedArray.getType(index)
    }

    fun hasValue(@StyleableRes index: Int): Boolean {
        return typedArray.hasValue(index)
    }

    fun peekValue(@StyleableRes index: Int): TypedValue {
        return typedArray.peekValue(index)
    }

    fun recycle() {
        typedArray.recycle()
    }
}
