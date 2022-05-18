package com.trufflear.trufflear.views.indicators

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.appcompat.content.res.AppCompatResources
import com.trufflear.trufflear.R
import com.trufflear.trufflear.views.TintUtils
import com.trufflear.trufflear.views.TypedArrayCompat

/**
 * Implementation of the Core UI Circular Progress Indicator.
 *
 * Available styles:
 *
 * - [`@style/CoreUiCircularProgressIndicator.Two`][R.style.CoreUiCircularProgressIndicator_Two]
 * - [`@style/CoreUiCircularProgressIndicator.Three`][R.style.CoreUiCircularProgressIndicator_Three]
 * - [`@style/CoreUiCircularProgressIndicator.Four`][R.style.CoreUiCircularProgressIndicator_Four] (default)
 * - [`@style/CoreUiCircularProgressIndicator.Five`][R.style.CoreUiCircularProgressIndicator_Five]
 * - [`@style/CoreUiCircularProgressIndicator.Six`][R.style.CoreUiCircularProgressIndicator_Six]
 *
 * Tint the drawable with [`app:progressIndicatorTint`][R.attr.progressIndicatorTint], [setTint], or [setTintList].
 *
 * These styles are preset sizes (the number refers to multiples of 8dp).
 *
 * Related links:
 *
 * - [Design documentation](https://go.lyft.net/lpl-progress-indicator)
 * - [Android documentation](https://go.lyft.net/lpl-android-progress-indicator)
 */
class CircularProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreUiCircularProgressIndicatorStyle,
    defStyleRes: Int = R.style.CoreUiCircularProgressIndicator_Four,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val progressIndicatorDrawable: Drawable
    private val progressIndicatorSize: Int
    private var isInitialized = false

    init {
        val ta =
            TypedArrayCompat.obtainStyledAttributes(context, attrs, R.styleable.CoreUiCircularProgressIndicator, defStyleAttr, defStyleRes)
        try {
            progressIndicatorDrawable =
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.design_core_ui_components_progress_indicator_avd_circular_indeterminate
                )!!.mutate()

            if (ta.hasValue(R.styleable.CoreUiCircularProgressIndicator_indicatorTint)) {
                setTintList(ta.getColorStateList(R.styleable.CoreUiCircularProgressIndicator_indicatorTint))
            }
            if (ta.hasValue(R.styleable.CoreUiCircularProgressIndicator_progressIndicatorTintMode)) {
                setTintMode(
                    TintUtils.parseTintMode(
                        ta.getInt(R.styleable.CoreUiCircularProgressIndicator_progressIndicatorTintMode, -1),
                        PorterDuff.Mode.SRC_IN
                    )
                )
            }

            progressIndicatorSize = ta.getDimensionPixelSize(R.styleable.CoreUiCircularProgressIndicator_indicatorSize, 0)
            progressIndicatorDrawable.setBounds(0, 0, progressIndicatorSize, progressIndicatorSize)
            progressIndicatorDrawable.callback = this

            isInitialized = true
        } finally {
            ta.recycle()
        }
    }

    /**
     * Specifies the tint color for the progress indicator.
     *
     * To clear the tint, pass `null` to [setTintList].
     *
     * @param tintColor Color to use for tinting the progress indicator.
     * @see setTintList
     */
    fun setTint(@ColorInt tintColor: Int) {
        progressIndicatorDrawable.mutate().setTint(tintColor)
    }

    /**
     * Specifies the tint color for this progress indicator as a color state list.
     *
     * @param tint Color state list to use for tinting this progress indicator, or `null` to clear the tint.
     * @see setTint
     */
    fun setTintList(tint: ColorStateList?) {
        progressIndicatorDrawable.mutate().setTintList(tint)
    }

    /**
     * Specifies the tint mode for this progress indicator.
     *
     * @param tintMode the tint mode to use to tint the progress indicator
     */
    fun setTintMode(tintMode: PorterDuff.Mode) {
        progressIndicatorDrawable.mutate().setTintMode(tintMode)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startAnimation()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimation()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (!isInitialized) return

        if (visibility == VISIBLE) {
            startAnimation()
        } else {
            stopAnimation()
        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        if (!isInitialized) return

        if (progressIndicatorDrawable.isStateful) {
            val changed = progressIndicatorDrawable.setState(drawableState)
            if (changed) {
                invalidate()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(progressIndicatorSize + paddingStart + paddingEnd, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(progressIndicatorSize + paddingTop + paddingBottom, MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        val tx = (width - progressIndicatorSize) / 2
        val ty = (height - progressIndicatorSize) / 2
        canvas.translate(tx.toFloat(), ty.toFloat())
        progressIndicatorDrawable.draw(canvas)
        canvas.restore()
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === progressIndicatorDrawable || super.verifyDrawable(who)
    }

    private fun startAnimation() {
        if (!(progressIndicatorDrawable as Animatable).isRunning) {
            progressIndicatorDrawable.start()
        }
    }

    private fun stopAnimation() {
        if ((progressIndicatorDrawable as Animatable).isRunning) {
            progressIndicatorDrawable.stop()
        }
    }
}
