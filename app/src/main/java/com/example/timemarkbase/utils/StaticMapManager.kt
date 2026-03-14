package com.example.timemarkbase.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.LruCache
import com.example.timemarkbase.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object StaticMapManager {

    private const val CACHE_DIR = "map_cache"

    private val memoryCache = object : LruCache<String, Bitmap>(20) {}

    suspend fun getStaticMap(
        context: Context,
        lat: Double,
        lng: Double,
        zoom: Int = 16,
        width: Int = 400,
        height: Int = 200,
        apiKey: String
    ): Bitmap? {
        val cacheKey = "${lat}_${lng}_${zoom}_${width}x$height"

        memoryCache.get(cacheKey)?.let {
            return it
        }

        val diskBitmap = loadFromDisk(context, cacheKey)
        if (diskBitmap != null) {
            memoryCache.put(cacheKey, diskBitmap)
            return diskBitmap
        }

        val bitmap = fetchFromNetwork(lat, lng, zoom, width, height, apiKey)
        Log.d("TAG::", "getStaticMap: $bitmap")

        bitmap?.let {
            memoryCache.put(cacheKey, it)
            saveToDisk(context, cacheKey, it)
        }

        return bitmap
    }

    private suspend fun fetchFromNetwork(
        lat: Double,
        lng: Double,
        zoom: Int,
        width: Int,
        height: Int,
        apiKey: String
    ): Bitmap? = withContext(Dispatchers.IO) {

        val url = BuildConfig.BASE_URL +
                "?center=$lat,$lng" +
                "&zoom=$zoom" +
                "&size=${width}x$height" +
                "&markers=$lat,$lng" +
                "&api_key=$apiKey"
        Log.d("TAG::", "url: $url")
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connect()

            val input = connection.inputStream
            BitmapFactory.decodeStream(input)

        } catch (e: Exception) {
            Log.d("TAG::", "$e")
            e.printStackTrace()
            null
        }
    }

    private fun getCacheDir(context: Context): File {
        val dir = File(context.cacheDir, CACHE_DIR)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun saveToDisk(context: Context, key: String, bitmap: Bitmap) {
        try {
            val file = File(getCacheDir(context), "$key.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out)
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadFromDisk(context: Context, key: String): Bitmap? {
        return try {
            val file = File(getCacheDir(context), "$key.png")
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath)
            else null
        } catch (e: Exception) {
            null
        }
    }
}