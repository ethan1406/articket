package com.trufflear.trufflear.views

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.annotation.Px
import androidx.core.view.ViewCompat
import com.trufflear.trufflear.R
import com.trufflear.trufflear.views.ArtistLinkButton.IconGravity
import kotlin.math.min

/**
 * Fork of the text-start icon support from [com.google.android.material.button.MaterialButton].
 */
internal class ArtistLinkButtonIconHelper(private val view: ArtistLinkButton) {

    private var icon: Drawable? = null
    private var iconGravity: IconGravity = IconGravity.START

    @Px
    private var iconPadding: Int = 0
    private var iconTint: ColorStateList? = null

    @Px
    private var iconLeft: Int = 0

    fun loadFromAttributes(ta: TypedArrayCompat) {
        // Parse icon
        iconPadding = ta.getDimensionPixelSize(R.styleable.CoreUiButton_iconPadding, 0)
        iconTint = ta.getColorStateList(R.styleable.CoreUiButton_iconTint)
        iconGravity = when (ta.getInteger(R.styleable.CoreUiButton_iconGravityValue, ICON_GRAVITY_START)) {
            ICON_GRAVITY_START      -> IconGravity.START
            ICON_GRAVITY_TEXT_START -> IconGravity.TEXT_START
            else                    -> throw IllegalArgumentException("Unknown icon gravity provided")
        }
        icon = ta.getDrawable(R.styleable.CoreUiButton_icon)

        view.compoundDrawablePadding = iconPadding
        updateIcon()
    }

    fun getIcon(): Drawable? {
        return icon
    }

    fun setIcon(icon: Drawable?) {
        if (this.icon != icon) {
            this.icon = icon
            updateIcon()
        }
    }

    fun getIconGravity(): IconGravity {
        return iconGravity
    }

    fun setIconGravity(iconGravity: IconGravity) {
        if (this.iconGravity != iconGravity) {
            this.iconGravity = iconGravity
            updateIcon()
        }
    }

    private fun updateIcon() {
        icon?.apply {
            mutate()
            setTintList(iconTint)
            setBounds(iconLeft, 0, iconLeft + intrinsicWidth, intrinsicHeight)
        }
        view.setCompoundDrawablesRelative(icon, null, null, null)
    }

    fun updateIconPosition() {
        val icon = icon
        if (icon == null || iconGravity != IconGravity.TEXT_START || view.layout == null) {
            return
        }

        val textPaint = view.paint
        var buttonText = view.text.toString()
        if (view.transformationMethod != null) {
            // If text is transformed, add that transformation to to ensure correct calculation
            // of icon padding.
            buttonText = view.transformationMethod.getTransformation(buttonText, view).toString()
        }

        val textWidth = min(textPaint.measureText(buttonText).toInt(), view.layout.width)

        var newIconLeft =
            (view.measuredWidth
                    - textWidth
                    - view.paddingEnd
                    - icon.intrinsicWidth
                    - iconPadding
                    - view.paddingStart) / 2

        if (isLayoutRtl()) {
            newIconLeft = -newIconLeft
        }

        if (iconLeft != newIconLeft) {
            iconLeft = newIconLeft
            updateIcon()
        }
    }

    private fun isLayoutRtl() = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL
}

private const val ICON_GRAVITY_START = 1
private const val ICON_GRAVITY_TEXT_START = 2