package com.example.timemarkbase.custom_view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import com.example.timemarkbase.utils.drawVerifiedText

class VerifiedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var verifiedId: String = "123456"

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Gọi hàm drawVerifiedText
        drawVerifiedText(
            canvas = canvas,
            x = width.toFloat(),
            y = height / 2f,
            verifiedId = verifiedId,
            textSize = 30f
        )
    }
}