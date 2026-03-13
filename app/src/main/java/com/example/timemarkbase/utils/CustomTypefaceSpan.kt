package com.example.timemarkbase.utils

import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomTypefaceSpan(private val typeface: Typeface) : TypefaceSpan("") {

    override fun updateDrawState(ds: TextPaint) {
        apply(ds)
    }

    override fun updateMeasureState(paint: TextPaint) {
        apply(paint)
    }

    private fun apply(paint: Paint) {
        paint.typeface = typeface
    }
}