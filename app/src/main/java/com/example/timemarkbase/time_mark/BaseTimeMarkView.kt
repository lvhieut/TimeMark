package com.example.timemarkbase.time_mark

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

abstract class BaseTimeMarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs), TimeMarkRenderer {

    protected val commonTypeface: Typeface by lazy {
        Typeface.create("sans-serif-condensed", Typeface.NORMAL)
    }

    protected fun styleTextViews(vararg views: TextView?) {
        views.forEach { view ->
            view?.typeface = commonTypeface
            view?.letterSpacing = 0.02f
            view?.setShadowLayer(3.5f, 0.6f, 0.6f, Color.parseColor("#90000000"))
        }
    }
}
