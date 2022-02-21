package com.example.arimage.views.indicators

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
import com.example.arimage.R
import com.example.arimage.views.TintUtils
import com.example.arimage.views.TypedArrayCompat

/**
 * Implementation of the Core UI Linear Progress Indicator.
 *
 * Available styles:
 *
 * - [`@style/CoreUiLinearProgressIndicator.Focus`][R.style.CoreUiLinearProgressIndicator_Focus] (default)
 * - [`@style/CoreUiLinearProgressIndicator.Drive`][R.style.CoreUiLinearProgressIndicator_Drive]
 *
 * Tint the drawable with [`app:progressIndicatorTint`][R.attr.progressIndicatorTint], [setTint], or [setTintList].
 *
 * Related links:
 *
 * - [Design documentation](https://go.lyft.net/lpl-progress-indicator)
 * - [Android documentation](https://go.lyft.net/lpl-android-progress-indicator)
 */
class LinearProgressIndicator @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreUiLinearProgressIndicatorStyle,
    defStyleRes: Int = R.style.CoreUiLinearProgressIndicator_Focus,
) : View(context, attrs, defStyleAttr, defStyleRes) {

    private val progressIndicatorDrawable: Drawable
    private val progressIndicatorSize: Int
    private var isInitialized = false

    init {
        val ta =
            TypedArrayCompat.obtainStyledAttributes(context, attrs, R.styleable.CoreUiLinearProgressIndicator, defStyleAttr, defStyleRes)
        try {
            progressIndicatorDrawable =
                AppCompatResources.getDrawable(
                    context,
                    R.drawable.design_core_ui_components_progress_indicator_avd_linear_indeterminate
                )!!.mutate()

            if (ta.hasValue(R.styleable.CoreUiLinearProgressIndicator_indicatorTint)) {
                setTintList(ta.getColorStateList(R.styleable.CoreUiLinearProgressIndicator_indicatorTint))
            }
            if (ta.hasValue(R.styleable.CoreUiLinearProgressIndicator_progressIndicatorTintMode)) {
                setTintMode(
                    TintUtils.parseTintMode(
                        ta.getInt(R.styleable.CoreUiLinearProgressIndicator_progressIndicatorTintMode, -1),
                        PorterDuff.Mode.SRC_IN
                    )
                )
            }

            progressIndicatorSize = ta.getDimensionPixelSize(R.styleable.CoreUiCircularProgressIndicator_indicatorSize, 0)
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

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        progressIndicatorDrawable.setBounds(paddingStart, paddingTop, w - paddingEnd, h - paddingBottom)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(progressIndicatorSize + paddingTop + paddingBottom, MeasureSpec.EXACTLY)
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        progressIndicatorDrawable.draw(canvas)
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
