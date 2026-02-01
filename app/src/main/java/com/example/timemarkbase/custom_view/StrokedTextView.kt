package com.example.timemarkbase.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Region
import android.os.Build
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.example.timemarkbase.R

@SuppressLint("CustomViewStyleable")
class StrokedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    AppCompatTextView(context, attrs, defStyle) {
    private val mOutlinePaint = Paint()
    private val mOutlinePath = Path()
    private val shadowPath = Path()
    var shadowPaint: Paint = Paint()

    private var _strokeWidth = 0f
    private var _strokeColor = 0f
    private var _hasShadow = false
    private val matrix = Matrix()


    init {
        if (attrs != null) {
            val a = context.obtainStyledAttributes(attrs, R.styleable.StrokedTextAttrs)
            _strokeColor = a.getColor(
                R.styleable.StrokedTextAttrs_textStrokeColor,
                currentTextColor
            ).toFloat()
            _hasShadow = a.getBoolean(
                R.styleable.StrokedTextAttrs_textHasShadow,
                false
            )
            _strokeWidth = a.getFloat(
                R.styleable.StrokedTextAttrs_textStrokeWidth,
                StrokedTextView.Companion.DEFAULT_STROKE_WIDTH
            )
            a.recycle()
        } else {
            _strokeColor = currentTextColor.toFloat()
            _strokeWidth = StrokedTextView.Companion.DEFAULT_STROKE_WIDTH
        }
        //convert values specified in dp in XML layout to
        //px, otherwise stroke width would appear different
        //on different screens
        _strokeWidth = StrokedTextView.Companion.dpToPx(context, _strokeWidth).toFloat()
        init()
    }

    private fun init() {
        mOutlinePaint.strokeWidth = _strokeWidth
        mOutlinePaint.style = Paint.Style.STROKE
        mOutlinePaint.color = _strokeColor.toInt()
        shadowPaint = Paint(mOutlinePaint)
        shadowPaint.alpha = 120
    }

    override fun onTextChanged(
        text: CharSequence,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        requestLayout()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        var xOffset = 0f
        var baseline = 0f
        try {
            xOffset = layout.getLineLeft(0) + paddingLeft
            baseline = (layout.getLineBaseline(0) + paddingTop).toFloat()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        paint.getTextPath(text.toString(), 0, text.length, xOffset, baseline, mOutlinePath)
        shadowPath.op(mOutlinePath, Path.Op.UNION)
        matrix.postTranslate(0f, 8f)
        shadowPath.transform(matrix)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (_strokeWidth > 0) {
            canvas.save()
            // The following insures that we don't draw inside the characters.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                canvas.clipPath(mOutlinePath, Region.Op.DIFFERENCE)
            } else {
                canvas.clipOutPath(mOutlinePath)
            }
            canvas.drawPath(mOutlinePath, mOutlinePaint)
            if (_hasShadow) {
                canvas.drawPath(shadowPath, shadowPaint)
            }
            canvas.restore()
        } else {
            if (_hasShadow) {
                canvas.drawPath(shadowPath, shadowPaint)
            }
        }
    }

    fun setStrokeWidth(strokeWidth: Float) {
        _strokeWidth = StrokedTextView.Companion.dpToPx(context, strokeWidth).toFloat()
        mOutlinePaint.strokeWidth = _strokeWidth
        invalidate()
    }

    fun setStrokeColor(color: Int) {
        _strokeColor = color.toFloat()
    }

    companion object {
        private const val DEFAULT_STROKE_WIDTH = 0f
        fun dpToPx(context: Context, dp: Float): Int {
            val scale = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }
}