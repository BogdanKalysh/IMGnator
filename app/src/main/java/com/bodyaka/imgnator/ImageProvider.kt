package com.bodyaka.imgnator

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.bodyaka.imgnator.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ImageProvider(contentResolver: ContentResolver) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val imageCursor = Utils.getMediaStoreImagesCursor(contentResolver)
    private val idColumn = imageCursor?.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

    private val imageUriList = mutableListOf<Uri>()

    init {
        scope.launch {
            imageCursor?.let {
                while (imageCursor.moveToNext()) {
                    if (idColumn != null) {
                        val id = imageCursor.getLong(idColumn)
                        Log.i(TAG, "Received image id: $id")

                        imageUriList.add(ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            id
                        ))
                    }
                }
            }
        }
    }

    fun receiveNextImageUri(): Uri? {
        if (imageUriList.size > 0) {
            val randomIndex = Random.nextInt(imageUriList.size)
            return imageUriList[randomIndex]
        } else {
            return null
        }
    }

    companion object {
        val TAG: String = ImageProvider::class.java.name
    }
}