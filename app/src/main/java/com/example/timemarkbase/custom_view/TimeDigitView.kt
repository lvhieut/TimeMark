package com.example.timemarkbase.custom_view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
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

        // Tính chiều rộng dựa vào time + ratio spacing
        val digitHeight = desiredHeight.toFloat()
        val spacing = digitHeight * 0.03f
        val colonWidth = digitHeight * 0.24f

        var totalWidth = 0f
        time.forEachIndexed { index, c ->
            totalWidth += if (c == ':') {
                colonWidth
            } else {
                val resId = context.resources.getIdentifier("num_$c", "drawable", context.packageName)
                if (resId != 0) {
                    val opt = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    android.graphics.BitmapFactory.decodeResource(context.resources, resId, opt)
                    digitHeight * (opt.outWidth.toFloat() / opt.outHeight)
                } else 0f
            }

            if (index < time.lastIndex) totalWidth += spacing
        }

        val finalWidth = totalWidth.toInt()
        val finalHeight = desiredHeight

        setMeasuredDimension(finalWidth, finalHeight)
    }
}