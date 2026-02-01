package com.example.timemarkbase.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.provider.Settings
import android.text.TextPaint
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.fragment.app.Fragment

fun View.slideAnimation(
    direction: SlideDirection,
    type: SlideType,
    duration: Long = 400,
    runnable: Runnable? = null
) {
    val fromX: Float
    val toX: Float
    val fromY: Float
    val toY: Float
    val array = IntArray(2)
    getLocationInWindow(array)
    if ((type == SlideType.HIDE && (direction == SlideDirection.RIGHT || direction == SlideDirection.DOWN)) || (type == SlideType.SHOW && (direction == SlideDirection.LEFT || direction == SlideDirection.UP))) {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val deviceWidth = displayMetrics.widthPixels
        val deviceHeight = displayMetrics.heightPixels
        array[0] = deviceWidth
        array[1] = deviceHeight
    }
    when (direction) {
        SlideDirection.UP -> {
            fromX = 0f
            toX = 0f
            fromY = if (type == SlideType.HIDE) 0f else (array[1] + height).toFloat()
            toY = if (type == SlideType.HIDE) -1f * (array[1] + height) else 0f
        }

        SlideDirection.DOWN -> {
            fromX = 0f
            toX = 0f
            fromY = if (type == SlideType.HIDE) 0f else -1f * (array[1] + height)
            toY = if (type == SlideType.HIDE) 1f * (array[1] + height) else 0f
        }

        SlideDirection.LEFT -> {
            fromX = if (type == SlideType.HIDE) 0f else 1f * (array[0] + width)
            toX = if (type == SlideType.HIDE) -1f * (array[0] + width) else 0f
            fromY = 0f
            toY = 0f
        }

        SlideDirection.RIGHT -> {
            fromX = if (type == SlideType.HIDE) 0f else -1f * (array[0] + width)
            toX = if (type == SlideType.HIDE) 1f * (array[0] + width) else 0f
            fromY = 0f
            toY = 0f
        }
    }
    val animate = TranslateAnimation(
        fromX, toX, fromY, toY
    )
    animate.duration = duration
    animate.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) {

        }

        override fun onAnimationEnd(animation: Animation?) {
            if (type == SlideType.HIDE) {
                visibility = View.GONE
            }
            runnable?.run()
        }

        override fun onAnimationStart(animation: Animation?) {
            if (type == SlideType.SHOW) {
                visibility = View.VISIBLE
            }
        }

    })
    startAnimation(animate)
}

@SuppressLint("HardwareIds")
fun Context.getAndroidID(): String {
    return Settings.Secure.getString(
        this.contentResolver,
        Settings.Secure.ANDROID_ID
    ) ?: "unknown_android_id"
}

fun drawTimeWithDigitPng(
    context: Context,
    canvas: Canvas,
    time: String,              // ví dụ "08:45"
    startX: Float,
    baseLineY: Float,          // y đáy của số
    digitHeight: Float,        // chiều cao số
    spacingRatio: Float = 0.03f
): Float {

    val spacing = digitHeight * spacingRatio
    val colonWidth = digitHeight * 0.24f

    var x = startX
    var totalWidth = 0f

    // ===== Pass 1: tính total width =====
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
                val opt = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeResource(context.resources, resId, opt)
                digitHeight * (opt.outWidth.toFloat() / opt.outHeight)
            } else 0f
        }

        if (index < time.lastIndex) {
            totalWidth += spacing
        }
    }

    // ===== Paint cho dấu : =====
    val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    // ===== Pass 2: vẽ từng ký tự =====
    time.forEachIndexed { index, c ->
        if (c == ':') {
            val dotWidth = digitHeight * 0.1f
            val dotHeight = digitHeight * 0.26f

            canvas.drawRect(
                x + dotWidth,
                baseLineY - digitHeight * 0.65f,
                x + dotWidth * 2,
                baseLineY - digitHeight * 0.45f,
                colonPaint
            )
            canvas.drawRect(
                x + dotWidth,
                baseLineY - digitHeight * 0.25f,
                x + dotWidth * 2,
                baseLineY - digitHeight * 0.05f,
                colonPaint
            )

            x += colonWidth
        } else {
            val resId = context.resources.getIdentifier(
                "num_$c",
                "drawable",
                context.packageName
            )

            if (resId != 0) {
                BitmapFactory.decodeResource(context.resources, resId)?.let { bmp ->
                    val w = digitHeight * (bmp.width.toFloat() / bmp.height)
                    canvas.drawBitmap(
                        bmp,
                        null,
                        RectF(x, baseLineY - digitHeight, x + w, baseLineY),
                        null
                    )
                    x += w
                }
            }
        }

        if (index < time.lastIndex) {
            x += spacing
        }
    }

    return totalWidth
}

fun drawVerifiedText(
    canvas: Canvas,
    x: Float,
    y: Float,
    verifiedId: String,
    textSize: Float = 40f
) {
    if (verifiedId.isEmpty()) return

    // Màu chữ
    var color = Color.parseColor("#EEEEEE")

    // TextPaint cho ID
    val textPaintId = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.textSize = textSize
        this.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        this.letterSpacing = 0.02f
        this.setShadowLayer(textSize * 0.016f, 0.3f, 0.3f, Color.parseColor("#50000000"))
    }

    // TextPaint cho "Timemark Verified"
    val textPaintLabel = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        this.textSize = textSize
        this.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        this.letterSpacing = 0.03f
        this.setShadowLayer(textSize * 0.016f, 0.3f, 0.3f, Color.parseColor("#50000000"))
    }

    // Chiều cao và rộng icon check
    val iconSize = textSize * 0.72f
    val padding = textSize * 0.35f

    // Tổng chiều dài
    val totalWidth = iconSize + padding + textPaintId.measureText(verifiedId) + textPaintLabel.measureText(" Timemark Verified")

    canvas.save()

    canvas.translate(x, y)
    canvas.rotate(-90f)

    // Tính vị trí icon
    val iconX = -(totalWidth / 2)
    val iconY = 0f

    // Vẽ icon Verified (2 path hình dấu tích)
    val paintIcon = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = iconSize * 0.14f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    val path1 = Path().apply {
        moveTo(iconX + iconSize * 0.0f, iconY - iconSize * 0.95f)
        lineTo(iconX + iconSize * 0.42f, iconY - iconSize * 0.85f)
        lineTo(iconX + iconSize * 0.42f, iconY - iconSize * 0.4f)
        quadTo(iconX + iconSize * 0.42f, iconY - iconSize * 0.35f, iconX, iconY - iconSize * 0.28f)
        quadTo(iconX - iconSize * 0.42f, iconY - iconSize * 0.35f, iconX - iconSize * 0.42f, iconY - iconSize * 0.4f)
        lineTo(iconX - iconSize * 0.42f, iconY - iconSize * 0.85f)
        close()
    }
    canvas.drawPath(path1, paintIcon)

    val path2 = Path().apply {
        moveTo(iconX - iconSize * 0.18f, iconY - iconSize * 0.45f)
        lineTo(iconX - iconSize * 0.02f, iconY - iconSize * 0.28f)
        lineTo(iconX + iconSize * 0.22f, iconY - iconSize * 0.62f)
    }
    canvas.drawPath(path2, paintIcon)

    // Vẽ text Verified ID
    val textX = iconX + iconSize + padding
    canvas.drawText(verifiedId, textX, iconY, textPaintId)

    // Vẽ text "Timemark Verified"
    val labelX = textX + textPaintId.measureText(verifiedId)
    canvas.drawText(" Timemark Verified", labelX, iconY, textPaintLabel)

    canvas.restore()
}