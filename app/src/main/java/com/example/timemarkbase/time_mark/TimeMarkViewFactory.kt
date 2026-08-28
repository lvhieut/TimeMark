package com.example.timemarkbase.time_mark

import android.content.Context

object TimeMarkViewFactory {
    fun create(context: Context, style: TimeMarkStyle): BaseTimeMarkView {
        return when (style) {
            TimeMarkStyle.CLASSIC -> ClassicTimeMarkView(context)
            TimeMarkStyle.COMPACT -> CompactTimeMarkView(context)
        }
    }
}
