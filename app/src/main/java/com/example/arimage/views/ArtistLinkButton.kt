package com.example.arimage.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Property
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.core.math.MathUtils
import androidx.core.widget.TextViewCompat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import com.example.arimage.R

/**
 * Implementation of a Core UI Button.
 *
 * Available styles:
 *
 * - [`@style/CoreUiButton.Focus.Primary`][R.style.CoreUiButton_Focus_Primary]
 * - [`@style/CoreUiButton.Focus.Primary.Elevated`][R.style.CoreUiButton_Focus_Primary_Elevated] (default)
 * - [`@style/CoreUiButton.Focus.Primary.Neutral`][R.style.CoreUiButton_Focus_Primary_Neutral]
 * - [`@style/CoreUiButton.Focus.Primary.Destructive`][R.style.CoreUiButton_Focus_Primary_Destructive]
 * - [`@style/CoreUiButton.Focus.Secondary`][R.style.CoreUiButton_Focus_Secondary]
 * - [`@style/CoreUiButton.Focus.Secondary.Neutral`][R.style.CoreUiButton_Focus_Secondary_Neutral]
 * - [`@style/CoreUiButton.Focus.Secondary.Destructive`][R.style.CoreUiButton_Focus_Secondary_Destructive]
 * - [`@style/CoreUiButton.Focus.Compact.Primary`][R.style.CoreUiButton_Focus_Compact_Primary]
 * - [`@style/CoreUiButton.Focus.Compact.Primary.Elevated`][R.style.CoreUiButton_Focus_Compact_Primary_Elevated]
 * - [`@style/CoreUiButton.Focus.Compact.Primary.Neutral`][R.style.CoreUiButton_Focus_Compact_Primary_Neutral]
 * - [`@style/CoreUiButton.Focus.Compact.Primary.Destructive`][R.style.CoreUiButton_Focus_Compact_Primary_Destructive]
 * - [`@style/CoreUiButton.Focus.Compact.Secondary`][R.style.CoreUiButton_Focus_Compact_Secondary]
 * - [`@style/CoreUiButton.Focus.Compact.Secondary.Neutral`][R.style.CoreUiButton_Focus_Compact_Secondary_Neutral]
 * - [`@style/CoreUiButton.Focus.Compact.Secondary.Destructive`][R.style.CoreUiButton_Focus_Compact_Secondary_Destructive]
 * - [`@style/CoreUiButton.Drive.Primary`][R.style.CoreUiButton_Drive_Primary]
 * - [`@style/CoreUiButton.Drive.Primary.Elevated`][R.style.CoreUiButton_Drive_Primary_Elevated]
 * - [`@style/CoreUiButton.Drive.Primary.Destructive`][R.style.CoreUiButton_Drive_Primary_Destructive]
 * - [`@style/CoreUiButton.Drive.Secondary`][R.style.CoreUiButton_Drive_Secondary]
 * - [`@style/CoreUiButton.Drive.Secondary.Neutral`][R.style.CoreUiButton_Drive_Secondary_Neutral]
 * - [`@style/CoreUiButton.Drive.Secondary.Destructive`][R.style.CoreUiButton_Drive_Secondary_Destructive]
 * - [`@style/CoreUiButton.Drive.Compact.Primary`][R.style.CoreUiButton_Drive_Compact_Primary]
 * - [`@style/CoreUiButton.Drive.Compact.Primary.Elevated`][R.style.CoreUiButton_Drive_Compact_Primary_Elevated]
 * - [`@style/CoreUiButton.Drive.Compact.Primary.Destructive`][R.style.CoreUiButton_Drive_Compact_Primary_Destructive]
 * - [`@style/CoreUiButton.Drive.Compact.Secondary`][R.style.CoreUiButton_Drive_Compact_Secondary]
 * - [`@style/CoreUiButton.Drive.Compact.Secondary.Neutral`][R.style.CoreUiButton_Drive_Compact_Secondary_Neutral]
 * - [`@style/CoreUiButton.Drive.Compact.Secondary.Destructive`][R.style.CoreUiButton_Drive_Compact_Secondary_Destructive]
 *
 * The background style can be further customized with the following attributes:
 *
 * - [`app:backgroundTint`][R.attr.backgroundTint]
 * - [`app:backgroundTintMode][R.attr.backgroundTintMode]
 * - [`app:rippleColor`][R.attr.rippleColor]
 * - [`app:strokeColor`][R.attr.strokeColor]
 * - [`app:strokeWidth`][R.attr.strokeWidth]
 *
 * Note that [ArtistLinkButton] does not support compound drawables set with `android:drawable***`.
 * To set a start drawable, use the following attributes:
 *
 * - [`app:icon`][R.attr.icon]
 * - [`app:iconPadding`][R.attr.iconPadding]
 * - [`app:iconTint`][R.attr.iconTint]
 * - [`app:iconGravity`[R.attr.iconGravity]
 *
 * Core UI icon size recommendations:
 * - Small for Focus Compact and Focus
 * - Medium for Drive Compact and Drive
 *
 * Do not use `wrap_content` as its `layout_width`, as it doesn't work nicely with text
 * auto-sizing, which [ArtistLinkButton] supports by default.
 *
 * Do not use `android:background` as [ArtistLinkButton] manages its own background, and settings a new
 * background means [ArtistLinkButton] cannot guarantee well-defined behavior.
 *
 * Related links:
 *
 * - [Design documentation](https://go.lyft.net/lpl-buttons)
 * - [Android documentation](https://go.lyft.net/lpl-android-buttons)
 */
class ArtistLinkButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.coreUiButtonStyle,
) : AppCompatButton(context, attrs, defStyleAttr) {

    companion object {

        /**
         * A Property wrapper around the [timerProgress] functionality.
         */
        @JvmField
        val TIMER_PROGRESS: Property<ArtistLinkButton, Float> = object : Property<ArtistLinkButton, Float>(Float::class.java, "timerProgress") {
            override fun get(d: ArtistLinkButton): Float {
                return d.timerProgress
            }

            override fun set(d: ArtistLinkButton, value: Float) {
                d.timerProgress = value
            }
        }
    }

    /**
     * Percentage value of the timer progress. Setting this to 0 clears the timer.
     */
    var timerProgress: Float = 0f
        get() = contentBeforeLoading?.timerProgress ?: field
        set(@FloatRange(from = 0.0, to = 1.0) value) {
            val newField = MathUtils.clamp(value, 0f, 1f)
            if (newField == field) return

            if (isLoading) {
                contentBeforeLoading?.timerProgress = newField
            } else {
                field = newField
                backgroundHelper?.setTimerProgress(field)
            }
        }

    /**
     * Indicates whether this view is in its loading state. When loading, the button is disabled to prevent further interactions.
     */
    var isLoading: Boolean = false
        set(value) {
            if (value == field) {
                return
            }
            if (value) {
                cacheAndClearButtonContent()
                progressDrawable.start()
                field = true
            } else {
                field = false
                restoreButtonContent()
                progressDrawable.stop()
            }
        }

    private val isInitialized: Boolean

    // Loading support
    private data class ButtonContent(
        var text: CharSequence?,
        var contentDescription: CharSequence?,
        var icon: Drawable?,
        var isEnabled: Boolean,
        var timerProgress: Float,
    )

    private var contentBeforeLoading: ButtonContent? = null
    private val progressDrawable: AnimatedVectorDrawableCompat

    // Text auto-sizing and max lines adjustment support
    private var textMinSize: Int = 0
    private var textMaxSize: Int = 0
    private var textSizeStepGranularity: Int = 0
    private var isTextAutoSizeEnabled: Boolean = false
    private var lastText: CharSequence? = null
    private var lastMaxLines: Int = 0

    private var backgroundHelper: ArtistLinkButtonBackgroundHelper? = null
    private val iconHelper: ArtistLinkButtonIconHelper = ArtistLinkButtonIconHelper(this)

    init {
        val ta = TypedArrayCompat.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.CoreUiButton,
            defStyleAttr,
            R.style.CoreUiButton_Focus_Primary_Elevated
        )
        try {
            backgroundHelper = ArtistLinkButtonBackgroundHelper(this).apply { loadFromAttributes(ta) }
            iconHelper.loadFromAttributes(ta)

            progressDrawable =
                AnimatedVectorDrawableCompat.create(
                    this.context,
                    R.drawable.progress_indicator_circular
                )!!.apply {
                    val tint = ta.getColorStateList(R.styleable.CoreUiButton_progressIndicatorTint)
                    val size = ta.getDimensionPixelSize(R.styleable.CoreUiButton_progressIndicatorSize, 0)
                    setBounds(0, 0, size, size)
                    mutate().setTintList(tint)
                    callback = this@ArtistLinkButton
                }
        } finally {
            ta.recycle()
        }

        initTextScalingAndWrapping(context)

        isInitialized = true
    }

    /**
     * Sets the icon for this button.
     *
     * Core UI icon size recommendations:
     * - Small for Focus Compact and Focus
     * - Medium for Drive Compact and Drive
     *
     * @param icon Drawable to use for the button's icon.
     *
     * @see setIconResource
     * @see setIconGravity
     */
    fun setIcon(icon: Drawable?) {
        if (isLoading) {
            contentBeforeLoading?.icon = icon
        } else {
            iconHelper.setIcon(icon)
            updateTextGravity()
        }
    }

    /**
     * Sets the icon for this button.
     *
     * Core UI icon size recommendations:
     * - Small for Focus Compact and Focus
     * - Medium for Drive Compact and Drive
     *
     * @param iconResourceId Drawable resource ID to use for the button's icon.
     *
     * @see setIcon
     * @see setIconGravity
     */
    fun setIconResource(@DrawableRes iconResourceId: Int) {
        setIcon(AppCompatResources.getDrawable(context, iconResourceId))
    }

    /**
     * Gets the icon gravity for this button.
     *
     * @see setIconGravity
     */
    fun getIconGravity(): IconGravity {
        return iconHelper.getIconGravity()
    }

    /**
     * Sets the icon gravity for this button.
     *
     * @param iconGravity icon gravity for this button
     * @see getIconGravity
     */
    fun setIconGravity(iconGravity: IconGravity) {
        iconHelper.setIconGravity(iconGravity)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isLoading) {
            canvas.save()
            val tx = (this.width - progressDrawable.bounds.width()) / 2f
            val ty = (this.height - progressDrawable.bounds.height()) / 2f
            canvas.translate(tx, ty)
            progressDrawable.draw(canvas)
            canvas.restore()
        }
    }

    override fun verifyDrawable(who: Drawable): Boolean {
        return who === progressDrawable || super.verifyDrawable(who)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return HapticUtils.onTouchEvent(this, event) || super.onTouchEvent(event)
    }

    override fun onTextChanged(text: CharSequence, start: Int, lengthBefore: Int, lengthAfter: Int) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)

        if (!isInitialized) return

        if (!isLoading) {
            iconHelper.updateIconPosition()
        }

        if (!isTextAutoSizeEnabled || TextUtils.equals(lastText, text)) {
            return
        }

        lastText = text

        adjustLines()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (!isLoading) {
            iconHelper.updateIconPosition()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isLoading) {
            progressDrawable.start()
        }
        backgroundHelper?.updateParentAbsoluteElevation(ElevationOverlayUtils.getParentAbsoluteElevation(this))
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressDrawable.stop()
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        backgroundHelper?.updateElevation(elevation)
    }

    override fun setEnabled(isEnabled: Boolean) {
        if (isLoading) {
            contentBeforeLoading?.isEnabled = isEnabled
        } else {
            super.setEnabled(isEnabled)
        }
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (isLoading) {
            contentBeforeLoading?.text = text
            super.setText(null, type)
        } else {
            super.setText(text, type)
        }
    }

    override fun setContentDescription(contentDescription: CharSequence?) {
        if (isLoading) {
            contentBeforeLoading?.contentDescription = contentDescription
        } else {
            super.setContentDescription(contentDescription)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)

        if (!isInitialized) return

        if (visibility == View.VISIBLE && isLoading) {
            progressDrawable.start()
        } else {
            progressDrawable.stop()
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        adjustLines()
    }

    @Suppress("DEPRECATION")
    override fun setBackgroundDrawable(background: Drawable?) {
        backgroundHelper?.let {
            when {
                it.isBackgroundOverwritten    -> {
                    super.setBackgroundDrawable(background)
                }
                background != getBackground() -> {
                    it.isBackgroundOverwritten = true
                    super.setBackgroundDrawable(background)
                }
            }
        } ?: super.setBackgroundDrawable(background)
    }

    internal fun setInternalBackground(drawable: Drawable?) {
        @Suppress("DEPRECATION")
        super.setBackgroundDrawable(drawable)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()

        if (!isInitialized) return

        if (progressDrawable.isStateful) {
            val changed = progressDrawable.setState(this.drawableState)
            if (changed) {
                invalidate()
            }
        }
    }

    override fun setBackgroundTintList(tint: ColorStateList?) {
        backgroundHelper?.let {
            if (it.isBackgroundOverwritten) {
                super.setBackgroundTintList(tint)
            } else {
                it.updateBackgroundTint(tint)
            }
        }
    }

    override fun getBackgroundTintList(): ColorStateList? {
        return backgroundHelper?.let {
            if (it.isBackgroundOverwritten) {
                super.getBackgroundTintList()
            } else {
                it.backgroundTint
            }
        } ?: super.getBackgroundTintList()
    }

    override fun setBackgroundTintMode(tintMode: PorterDuff.Mode?) {
        backgroundHelper?.let {
            if (it.isBackgroundOverwritten) {
                super.setBackgroundTintMode(tintMode)
            } else {
                it.updateBackgroundTintMode(tintMode)
            }
        }
    }

    override fun getBackgroundTintMode(): PorterDuff.Mode? {
        return backgroundHelper?.let {
            if (it.isBackgroundOverwritten) {
                super.getBackgroundTintMode()
            } else {
                it.backgroundTintMode
            }
        } ?: super.getBackgroundTintMode()
    }

    private fun adjustLines() {
        if (!isTextAutoSizeEnabled) {
            return
        }

        // only allow wrapping if text is ellipsized
        if (isEllipsizedWithSingleLine(text)) {
            setTwoLineWithFixedTextSize()
            if (lastMaxLines != 2) {
                lastMaxLines = 2
                // re-trigger layout
                text = text
            }
        } else {
            setSingleLineWithAutoSizing()
            if (lastMaxLines != 1) {
                lastMaxLines = 1
                // re-trigger layout
                text = text
            }
        }
        updateTextGravity()
    }

    private fun setSingleLineWithAutoSizing() {
        maxLines = 1
        if (textMinSize < textMaxSize) {
            TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, textMinSize, textMaxSize, textSizeStepGranularity,
                TypedValue.COMPLEX_UNIT_PX
            )
        }
    }

    private fun setTwoLineWithFixedTextSize() {
        maxLines = 2
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textMinSize.toFloat())
        TextViewCompat.setAutoSizeTextTypeWithDefaults(this, TextViewCompat.AUTO_SIZE_TEXT_TYPE_NONE)
    }

    private fun isEllipsizedWithSingleLine(text: CharSequence): Boolean {
        if (text.isEmpty()) {
            return false
        }
        if (text.toString().contains("\n")) {
            return true
        }

        val ellipsized = ellipsizeIfPossible(text)
        return !TextUtils.equals(ellipsized, text)
    }

    private fun ellipsizeIfPossible(text: CharSequence): CharSequence {
        val iconWidth = compoundDrawablesRelative[0]?.bounds?.width() ?: 0
        val availWidth = width - paddingLeft - paddingRight - iconWidth - compoundDrawablePadding
        val minTextSizePaint = TextPaint(paint).apply { textSize = textMinSize.toFloat() }
        return TextUtils.ellipsize(text, minTextSizePaint, availWidth.toFloat(), TextUtils.TruncateAt.END)
    }

    private fun initTextScalingAndWrapping(context: Context) {
        isTextAutoSizeEnabled = TextViewCompat.getAutoSizeTextType(this) == TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM

        if (!isTextAutoSizeEnabled) {
            return
        }

        textMinSize = TextViewCompat.getAutoSizeMinTextSize(this)
        textMaxSize = TextViewCompat.getAutoSizeMaxTextSize(this)
        textSizeStepGranularity = TextViewCompat.getAutoSizeStepGranularity(this)
        if (textSizeStepGranularity < 0) {
            textSizeStepGranularity =
                context.resources.getDimensionPixelSize(R.dimen.design_core_ui_components_button_text_auto_size_granularity)
        }
    }

    private fun updateTextGravity() {
        gravity = if (iconHelper.getIcon() != null && maxLines >= 2) {
            Gravity.START or Gravity.CENTER_VERTICAL
        } else {
            Gravity.CENTER
        }
    }

    private fun cacheAndClearButtonContent() {
        check(contentBeforeLoading == null) { "Pre-loading button state was not restored" }

        contentBeforeLoading =
            ButtonContent(text, contentDescription, iconHelper.getIcon(), isEnabled, timerProgress)

        text = null
        contentDescription = resources.getString(R.string.button_loading_content_description)
        iconHelper.setIcon(null)
        isEnabled = false
        timerProgress = 0f
    }

    private fun restoreButtonContent() {
        contentBeforeLoading?.let {
            text = it.text
            contentDescription = it.contentDescription
            setIcon(it.icon)
            isEnabled = it.isEnabled
            timerProgress = it.timerProgress
        } ?: throw IllegalStateException("Cannot restore button state to pre-loading")

        contentBeforeLoading = null
    }

    /** Positions the icon can be set to. */
    enum class IconGravity {

        /**
         * Gravity used to position the icon at the start of the view.
         *
         * @see getIconGravity
         * @see setIconGravity
         */
        START,

        /**
         * Gravity used to position the icon in the center of the view at the start of the text.
         *
         * @see getIconGravity
         * @see setIconGravity
         */
        TEXT_START,
    }
}
