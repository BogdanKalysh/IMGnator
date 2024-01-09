package com.bodyaka.imgnator.utils

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.provider.MediaStore
import android.util.Log

object Utils {
    private val TAG = this::class.java.name

    fun getMediaStoreImagesCursor(contentResolver: ContentResolver) : Cursor? {
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

        return contentResolver.query(
            collection,
            arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.RELATIVE_PATH,
                MediaStore.Images.Media.DISPLAY_NAME
            ),
            null,
            null,
            "${MediaStore.Images.Media.DATE_TAKEN} ASC"
        )
    }

    fun getGradientDrawableFromBitmap(bitmap: Bitmap): GradientDrawable {
        val scanHeight = bitmap.height / 10
        val pixelsContainer = IntArray(bitmap.width * scanHeight)
        bitmap.getPixels(pixelsContainer, 0, bitmap.width, 0, 0, bitmap.width, scanHeight)
        val topAverageColor = getAverageColorInt(pixelsContainer)
        bitmap.getPixels(pixelsContainer, 0, bitmap.width, 0, bitmap.height - scanHeight, bitmap.width, scanHeight)
        val bottomAverageColor = getAverageColorInt(pixelsContainer)

        Log.e(TAG, "Average colors for gradient: TOP=$topAverageColor, BOTTOM=$bottomAverageColor")

        val shape = GradientDrawable()
        shape.shape = GradientDrawable.RECTANGLE
        shape.colors = intArrayOf(topAverageColor, bottomAverageColor)

        return shape
    }

    private fun getAverageColorInt(colors: IntArray): Int {
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L

        for (color in colors) {
            rSum += color shr 16 and 0xff
            gSum += color shr 8 and 0xff
            bSum += color and 0xff
        }

        val r = (rSum / colors.size).toInt()
        val g = (gSum / colors.size).toInt()
        val b = (bSum / colors.size).toInt()

        return Color.rgb(r, g, b)
    }

    // Left just as example for future projects
//    private fun getAverageColor(colors: IntArray): Color {
//        var rSum = 0L
//        var gSum = 0L
//        var bSum = 0L
//
//        for (color in colors) {
//            rSum += color shr 16 and 0xff
//            gSum += color shr 8 and 0xff
//            bSum += color and 0xff
//        }
//
//        val r = (rSum / colors.size).toFloat()
//        val g = (gSum / colors.size).toFloat()
//        val b = (bSum / colors.size).toFloat()
//
//        return Color.valueOf(r, g, b)
//    }
}