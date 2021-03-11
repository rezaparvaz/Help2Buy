package com.bitvale.switcher

import android.animation.AnimatorSet
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.bitvale.switcher.commons.KEY_CHECKED
import com.bitvale.switcher.commons.STATE
import com.bitvale.switcher.commons.isLollipopAndAbove
import com.bitvale.switcher.commons.toPx
import kotlin.math.min


abstract class Switcher @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    protected open var iconRadius = 0f
    protected open var iconClipRadius = 0f
    protected open var iconCollapsedWidth = 0f
    protected open var defHeight = 0
    protected open var defWidth = 0
    open var isChecked = true
        protected set

    @ColorInt
    open var onColor = 0
    @ColorInt
    open var offColor = 0
    @ColorInt
    protected open var iconColor = 0

    protected open val switcherPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected open val iconRect = RectF(0f, 0f, 0f, 0f)
    protected open val iconClipRect = RectF(0f, 0f, 0f, 0f)
    protected open val iconPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected open val iconClipPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    protected open var animatorSet: AnimatorSet? = AnimatorSet()

    protected open val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    protected open var shadow: Bitmap? = null
    protected open var shadowOffset = 0f

    @ColorInt
    protected open var currentColor = 0
        set(value) {
            field = value
            switcherPaint.color = value
            iconClipPaint.color = value
        }

    protected open var switchElevation = 0f
    protected open var iconHeight = 0f

    protected open var iconProgress = 0f

    init {
        attrs?.let { retrieveAttributes(attrs, defStyleAttr) }
        setOnClickListener { setChecked(!isChecked) }
    }

    protected open fun retrieveAttributes(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.Switcher,
                defStyleAttr,
                R.style.Switcher
        )

        switchElevation = typedArray.getDimension(R.styleable.Switcher_elevation, 0f)

        onColor = typedArray.getColor(R.styleable.Switcher_switcher_on_color, 0)
        offColor = typedArray.getColor(R.styleable.Switcher_switcher_off_color, 0)
        iconColor = typedArray.getColor(R.styleable.Switcher_switcher_icon_color, 0)

        isChecked = typedArray.getBoolean(R.styleable.Switcher_android_checked, true)

        if (!isChecked) iconProgress = 1f

        currentColor = if (isChecked) onColor else offColor

        iconPaint.color = iconColor

        defHeight = typedArray.getDimensionPixelOffset(R.styleable.Switcher_switcher_height, 0)
        defWidth = typedArray.getDimensionPixelOffset(R.styleable.Switcher_switcher_width, 0)

        typedArray.recycle()

        if (!isLollipopAndAbove() && switchElevation > 0f) {
            shadowPaint.colorFilter = PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN)
            shadowPaint.alpha = 51 // 20%
            setShadowBlurRadius(switchElevation)
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    abstract override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)

    abstract override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int)

    abstract fun generateShadow()

    open fun applyColorChange() {
        animatorSet?.cancel()
        if (!isChecked) {
            currentColor = offColor
            iconProgress = 1f
        } else {
            currentColor = onColor
            iconProgress = 0f
        }
    }

    abstract override fun onDraw(canvas: Canvas?)

    abstract fun animateSwitch()

    protected open var listener: ((isChecked: Boolean) -> Unit)? = null

    /**
     * Register a callback to be invoked when the isChecked state of this switch
     * changes.
     *
     * @param listener the callback to call on isChecked state change
     */
    open fun setOnCheckedChangeListener(listener: (isChecked: Boolean) -> Unit) {
        this.listener = listener
    }

    /**
     * <p>Changes the isChecked state of this switch.</p>
     *
     * @param checked true to check the switch, false to uncheck it
     * @param withAnimation use animation
     */
    abstract fun setChecked(checked: Boolean, withAnimation: Boolean = true)

    override fun onSaveInstanceState(): Parcelable {
        super.onSaveInstanceState()
        return Bundle().apply {
            putBoolean(KEY_CHECKED, isChecked)
            putParcelable(STATE, super.onSaveInstanceState())
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(STATE))
            isChecked = state.getBoolean(KEY_CHECKED)
            if (!isChecked) forceUncheck()
        }
    }

    abstract fun forceUncheck()

    protected open fun setShadowBlurRadius(elevation: Float) {
        val maxElevation = context.toPx(24f)
        switchElevation = min(25f * (elevation / maxElevation), 25f)
    }
}