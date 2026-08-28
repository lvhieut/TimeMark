package com.example.timemarkbase.time_mark

import android.content.Context
import kotlin.math.roundToInt

data class TemplateItemSize(
    val widthPx: Int,
    val heightPx: Int,
    val previewHeightPx: Int,
    val textSizeSp: Float,
    val paddingPx: Int
)

object TemplateItemSizeResolver {
    fun resolve(context: Context): TemplateItemSize {
        val density = context.resources.displayMetrics.density
        val screenDp = context.resources.displayMetrics.widthPixels / density
        val widthDp = when {
            screenDp < 360f -> 88f
            screenDp < 420f -> 104f
            else -> 120f
        }
        val heightDp = when {
            screenDp < 360f -> 88f
            screenDp < 420f -> 98f
            else -> 108f
        }
        val previewDp = when {
            screenDp < 360f -> 48f
            screenDp < 420f -> 58f
            else -> 68f
        }
        val paddingDp = if (screenDp < 360f) 6f else 8f
        val textSize = if (screenDp < 360f) 11f else 12f

        fun dp(value: Float) = (value * density).roundToInt()

        return TemplateItemSize(
            widthPx = dp(widthDp),
            heightPx = dp(heightDp),
            previewHeightPx = dp(previewDp),
            textSizeSp = textSize,
            paddingPx = dp(paddingDp)
        )
    }
}
