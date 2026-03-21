package com.example.timemarkbase.custom_view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.timemarkbase.utils.TIME_COLON_SIDE_SPACING_RATIO
import com.example.timemarkbase.utils.TIME_COLON_WIDTH_RATIO
import com.example.timemarkbase.utils.TIME_SPACING_RATIO
import com.example.timemarkbase.utils.drawTimeWithDigitPng

class TimeDigitView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private var time: String = "00:00"

    fun setTime(value: String) {
        time = value
        requestLayout()
        invalidate()
    }

    fun getTime(): String = time

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val digitHeight = height.toFloat()
        drawTimeWithDigitPng(
            context,
            canvas,
            time,
            0f,
            height.toFloat(),
            digitHeight
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredHeight = MeasureSpec.getSize(heightMeasureSpec).takeIf { it > 0 } ?: 50

        val digitHeight = desiredHeight.toFloat()
        val spacing = digitHeight * TIME_SPACING_RATIO
        val colonWidth = digitHeight * TIME_COLON_WIDTH_RATIO
        val colonSideSpacing = digitHeight * TIME_COLON_SIDE_SPACING_RATIO

        fun getSpacingAfter(index: Int): Float {
            if (index >= time.lastIndex) return 0f
            val currentChar = time[index]
            val nextChar = time[index + 1]
            return if (currentChar == ':' || nextChar == ':') {
                colonSideSpacing
            } else {
                spacing
            }
        }

        var totalWidth = 0f
        time.forEachIndexed { index, c ->
            totalWidth += if (c == ':') {
                colonWidth
            } else {
                val resId = context.resources.getIdentifier(
                    "num_$c",
                    "drawable",
                    context.packageName
                )
                if (resId != 0) {
                    val opt = android.graphics.BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    android.graphics.BitmapFactory.decodeResource(context.resources, resId, opt)
                    digitHeight * (opt.outWidth.toFloat() / opt.outHeight)
                } else {
                    0f
                }
            }

            totalWidth += getSpacingAfter(index)
        }

        setMeasuredDimension(totalWidth.toInt(), desiredHeight)
    }
}