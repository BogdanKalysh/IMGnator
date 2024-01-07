package com.bodyaka.imgnator

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bodyaka.imgnator.databinding.ActivityMainBinding
import com.bodyaka.imgnator.utils.Utils.getBitmapTopAndBottomAverageColors
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageProvider: ImageProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        imageProvider = ImageProvider(contentResolver)

        binding.generateButton.setOnClickListener {
            if (isStorageAccessPermissionGranted()) {
                updateImageView(imageProvider.receiveNextImageUri())
            } else {
                requestStorageAccessPermissions()
            }
        }
    }

    private fun updateImageView(uri: Uri?) {
        if (uri == null) {
            Log.v(TAG, "Image URI is null can't update the ImageView")
            return
        } else {
            // TODO finish image setting
//            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri))
//            } else {
//                MediaStore.Images.Media.getBitmap(contentResolver, uri)
//            }
//
//            val imagePath = uri.path
//            imagePath?.let {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                    val bitmap = ThumbnailUtils.createImageThumbnail(File(imagePath), Size(500,500), null)
//                } else {
//                    val bitmap = ThumbnailUtils.createImageThumbnail(imagePath, MediaStore.Images.Thumbnails.MINI_KIND)
//                }
//            }
//
//
//            val (topColor, bottomColor) = getBitmapTopAndBottomAverageColors(
//                bitmap.copy(Bitmap.Config.RGBA_F16, true))
//            Log.e(TAG, "TOP: $topColor; BOTTOM: $bottomColor")
//
//            binding.mainPhotoImageview.setImageBitmap(bitmap)
        }
    }

    private fun isStorageAccessPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStorageAccessPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_IMAGES), ACCESS_PERMISSION_REQUEST_CODE)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), ACCESS_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (isStorageAccessPermissionGranted()) {
            Log.i(TAG, "Storage access permission was granted!")
            imageProvider = ImageProvider(contentResolver)
            updateImageView(imageProvider.receiveNextImageUri())
        } else {
            Log.v(TAG, "Storage access permission was denied :(")
            Toast.makeText(this, resources.getString(R.string.ask_to_grant_permission_text),
                Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        val TAG: String = MainActivity::class.java.name
        const val ACCESS_PERMISSION_REQUEST_CODE = 2306
    }
}