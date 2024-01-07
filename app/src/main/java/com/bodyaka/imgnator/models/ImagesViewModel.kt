package com.bodyaka.imgnator.models

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.DecodeException
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.bodyaka.imgnator.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Integer.min
import java.util.LinkedList
import java.util.Queue

class ImagesViewModel(val contentResolver: ContentResolver): ViewModel() {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val imagesDataCache = mutableListOf<Image>()
    private val bitmapsCache: Queue<Pair<Bitmap, Image>> = LinkedList()

    private var lastCachedImageId = 0

    private val _currentImage: MutableLiveData<Bitmap> = MutableLiveData<Bitmap>()
    val currentImage: LiveData<Bitmap> = _currentImage

    init {
        cacheURIsAndBitmaps()
    }

    fun updateCurrentImage() {
        Log.e(TAG, "UPDATE\n")
        if (bitmapsCache.size == 0) {
            Log.i(TAG, "Bitmap queue is empty. Caching bitmap on current thread.")
            cacheNextBitmap()
        } else {
            scope.launch {
                cacheNextBitmap()
            }
        }
        Log.e(TAG, "Setting image: ${bitmapsCache.peek()?.second}")
        _currentImage.value = bitmapsCache.remove().first
    }

    fun cacheURIsAndBitmaps() {
        scope.launch {
            val imageCursor = Utils.getMediaStoreImagesCursor(contentResolver)
            val idColumn = imageCursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val pathColumn = imageCursor?.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            val nameColumn = imageCursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)

            Log.d(TAG, "Caching image URIs")

            while (imageCursor!!.moveToNext()) {
                val uri = imageCursor.getLong(idColumn!!)
                val path = imageCursor.getString(pathColumn!!) + imageCursor.getString(nameColumn!!)
                imagesDataCache.add(
                    Image(
                        ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            uri
                        ), path
                    )
                )
            }

            Log.d(TAG, "Shuffling image URIs")
            imagesDataCache.shuffle()

            Log.d(TAG, "Start caching image bitmaps")
            repeat(min(BITMAP_CACHE_SIZE, imagesDataCache.size)) {
                cacheNextBitmap()
            }
            Log.d(TAG, "Finished caching image bitmaps")
        }
    }

    private fun cacheNextBitmap() {
        Log.i(TAG, "Caching image #$lastCachedImageId Img[${imagesDataCache[lastCachedImageId]}]")

        val imageData = imagesDataCache[lastCachedImageId++]

        @Suppress("DEPRECATION") // using getBitmap this to support older APIs
        val bitmap = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            MediaStore.Images.Media.getBitmap(contentResolver, imageData.uri)
        } else {
            val source = ImageDecoder.createSource(contentResolver, imageData.uri)
            try {
                ImageDecoder.decodeBitmap(source)
            } catch (e : DecodeException) {
                Log.e(TAG, "Could not decode image by path: ${imageData.path}")
                scope.launch {
                    cacheNextBitmap()
                }
                return
            }
        }
        bitmapsCache.add(bitmap to imageData)

        // reshuffling images when all were showed
        if (lastCachedImageId >= imagesDataCache.size) {
            imagesDataCache.shuffle()
            lastCachedImageId = 0
        }
    }

    companion object {
        val TAG: String = ImagesViewModel::class.java.name
        private const val BITMAP_CACHE_SIZE = 5

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val myContentResolver = this[APPLICATION_KEY]!!.contentResolver
                ImagesViewModel(
                    contentResolver = myContentResolver
                )
            }
        }
    }
}