package com.bodyaka.imgnator

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bodyaka.imgnator.databinding.ActivityMainBinding
import com.bodyaka.imgnator.models.ImagesViewModel
import com.bodyaka.imgnator.utils.Utils.getGradientDrawableFromBitmap


class MainActivity : AppCompatActivity() {
    private val imagesViewModel: ImagesViewModel by viewModels { ImagesViewModel.Factory }
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        imagesViewModel.currentImage.observe(this) {
            binding.mainPhotoImageview.setImageBitmap(it)

            val gradientBackground = getGradientDrawableFromBitmap(it)
            val r = resources.getDimension(R.dimen.main_corner_radius)
            gradientBackground.cornerRadii = FloatArray(8) {r}
            binding.mainPhotoImageview.background = gradientBackground

            if (it != null) {
                binding.placeholderCallToActionIcon.visibility = View.GONE
            } else {
                binding.placeholderCallToActionIcon.visibility = View.VISIBLE
            }
        }

        binding.generateButton.setOnClickListener {
            if (isStorageAccessPermissionGranted()) {
                // TODO When you give the permission manually and come back to app
                // View model has empty cache, because it didnt know that it had to try to cache again
                imagesViewModel.updateCurrentImage()
            } else {
                requestStorageAccessPermissions()
            }
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
            imagesViewModel.cacheURIsAndBitmaps()
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