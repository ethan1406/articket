package com.example.arimage.views

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RippleDrawable
import android.view.Gravity
import androidx.annotation.FloatRange
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import com.example.arimage.R
import com.google.android.material.shape.MaterialShapeDrawable

/**
 * Fork of the background styling support from [com.google.android.material.button.MaterialButtonHelper].
 */
internal class ArtistLinkButtonBackgroundHelper(
    private val button: ArtistLinkButton,
) {

    var backgroundTint: ColorStateList? = null
        private set
    var backgroundTintMode: PorterDuff.Mode? = null
        private set
    var isBackgroundOverwritten: Boolean = false
        set(value) {
            field = value
            button.backgroundTintList = backgroundTint
            button.backgroundTintMode = backgroundTintMode
        }

    private lateinit var baseDrawable: MaterialShapeDrawable
    private val maskDrawable = GradientDrawable()
    private var timerClipDrawable: ClipDrawable? = null

    fun loadFromAttributes(ta: TypedArrayCompat) {
        backgroundTint = ta.getColorStateList(R.styleable.CoreUiButton_backgroundTint)
        backgroundTintMode = TintUtils.parseTintMode(ta.getInt(R.styleable.CoreUiButton_backgroundTintMode, -1), PorterDuff.Mode.SRC_IN)
        val cornerRadius = ta.getDimension(R.styleable.CoreUiButton_cornerRadius, 0f) + CORNER_RADIUS_ADJUSTMENT
        val strokeColor = ta.getColorStateList(R.styleable.CoreUiButton_strokeColor)
        val strokeWidth = ta.getDimensionPixelSize(R.styleable.CoreUiButton_strokeWidth, 0)
        val timerTint = ta.getColorStateList(R.styleable.CoreUiButton_timerTint)
        val rippleColor = ta.getColorStateList(R.styleable.CoreUiButton_rippleColor)
            ?: AppCompatResources.getColorStateList(button.context, R.color.design_core_ui_pressed_low)!!

        baseDrawable = MaterialShapeDrawable.createWithElevationOverlay(button.context, button.elevation).apply {
            setCornerSize(cornerRadius)
        }

        updateTintAndTintMode()

        val progressBarDrawable = GradientDrawable()
        if (timerTint != null) {
            progressBarDrawable.cornerRadius = cornerRadius
            progressBarDrawable.color = timerTint
            timerClipDrawable = ClipDrawable(progressBarDrawable, Gravity.START, ClipDrawable.HORIZONTAL)
        }

        maskDrawable.cornerRadius = cornerRadius
        maskDrawable.setColor(AttrResolver.getColor(button.context, com.lyft.android.design.coreui.R.attr.coreUiSurfacePrimary))

        val drawables = mutableListOf<Drawable>(baseDrawable)
        timerClipDrawable?.let { drawables.add(it) }

        if (strokeWidth > 0 && strokeColor != null) {
            val strokeDrawable = GradientDrawable()
            strokeDrawable.cornerRadius = cornerRadius
            strokeDrawable.setColor(Color.TRANSPARENT)
            strokeDrawable.setStroke(strokeWidth, strokeColor)
            drawables.add(strokeDrawable)
        }

        button.setInternalBackground(
            RippleDrawable(
                rippleColor,
                InsetDrawable(LayerDrawable(drawables.toTypedArray()), 0),
                maskDrawable
            )
        )
    }

    fun updateBackgroundTint(tint: ColorStateList?) {
        if (tint != backgroundTint) {
            backgroundTint = tint
            updateTintAndTintMode()
        }
    }

    fun updateBackgroundTintMode(tintMode: PorterDuff.Mode?) {
        if (tintMode != backgroundTintMode) {
            backgroundTintMode = tintMode
            updateTintAndTintMode()
        }
    }

    fun setTimerProgress(@FloatRange(from = 0.0, to = 1.0) percentage: Float) {
        timerClipDrawable?.let {
            it.level = (DRAWABLE_MAX_LEVEL * percentage).toInt()
        }
    }

    private fun updateTintAndTintMode() {
        baseDrawable.mutate()
        baseDrawable.tintList = backgroundTint
        backgroundTintMode?.let { baseDrawable.setTintMode(it) }
    }

    fun updateParentAbsoluteElevation(@Px parentAbsoluteElevation: Float) {
        baseDrawable.parentAbsoluteElevation = parentAbsoluteElevation
    }

    fun updateElevation(@Px elevation: Float) {
        baseDrawable.elevation = elevation
    }
}

// This is a workaround. Currently on certain devices/versions,
// LayerDrawable will draw a black background underneath any layer with a non-opaque color,
// unless we set the shape to be something that's not a perfect rectangle.
private const val CORNER_RADIUS_ADJUSTMENT = 0.00001f

private const val DRAWABLE_MAX_LEVEL = 10000
