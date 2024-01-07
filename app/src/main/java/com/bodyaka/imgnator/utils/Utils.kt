package com.bodyaka.imgnator.utils

import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.provider.MediaStore

object Utils {
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

    fun getBitmapTopAndBottomAverageColors(bitmap: Bitmap): Pair<Color, Color> {
        val verticalLimit = bitmap.height / 3

        return getAverageColorFromBitmap(bitmap, 0, verticalLimit) to
                getAverageColorFromBitmap(bitmap, bitmap.height - verticalLimit, bitmap.height)
    }

    private fun getAverageColorFromBitmap(bitmap: Bitmap, topLimit: Int, bottomLimit: Int): Color {
        var rSum = 0L
        var gSum = 0L
        var bSum = 0L

        val pixelCount = bitmap.width * (bottomLimit - topLimit)

        repeat(bitmap.width) { x ->
            for (y in topLimit until bottomLimit) {
                val pixel: Int = bitmap.getPixel(x,y)

                rSum += pixel shr 16 and 0xff
                gSum += pixel shr 8 and 0xff
                bSum += pixel and 0xff
            }
        }

        val colorInt = Color.rgb(
            (rSum / pixelCount).toInt(),
            (gSum / pixelCount).toInt(),
            (bSum / pixelCount).toInt()
        )

        return Color.valueOf(colorInt)
    }
}