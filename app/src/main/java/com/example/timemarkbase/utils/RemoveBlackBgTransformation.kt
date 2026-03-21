package com.example.timemarkbase.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class RemoveBlackBgTransformation : BitmapTransformation() {

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update("remove_black_bg".toByteArray())
    }

    override fun transform(
        pool: BitmapPool,
        toTransform: Bitmap,
        outWidth: Int,
        outHeight: Int
    ): Bitmap {

        val width = toTransform.width
        val height = toTransform.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val threshold = 40

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = toTransform.getPixel(x, y)

                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                if (r < threshold && g < threshold && b < threshold) {
                    result.setPixel(x, y, Color.TRANSPARENT)
                } else {
                    result.setPixel(x, y, pixel)
                }
            }
        }

        return result
    }
}