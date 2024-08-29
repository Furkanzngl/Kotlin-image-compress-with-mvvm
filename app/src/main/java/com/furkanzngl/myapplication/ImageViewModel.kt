package com.furkanzngl.myapplication

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class ImageViewModel(
    private val imageRepository: ImageRepository,
    private val context: Context
) : ViewModel() {

    private val _compressedImage = MutableLiveData<Bitmap>()
    val compressedImage: LiveData<Bitmap> get() = _compressedImage

    // Sadece sıkıştırma
    fun compressImage(image: Bitmap, quality: Int) {
        viewModelScope.launch {
            val compressed = imageRepository.compressImage(image, quality)
            _compressedImage.postValue(compressed)
        }
    }

    // Sıkıştırma ve yeniden boyutlandırma
    fun compressAndResizeAndSaveImage(image: Bitmap, quality: Int, width: Int, height: Int): File? {
        var savedFile: File? = null
        viewModelScope.launch {
            val resizedAndCompressedImage = imageRepository.compressAndResizeImage(image, quality, width, height)
            _compressedImage.postValue(resizedAndCompressedImage)

            // Sıkıştırılmış ve yeniden boyutlandırılmış görüntüyü dosyaya kaydet
            savedFile = saveCompressedImageToFile(resizedAndCompressedImage, quality)
        }
        return savedFile
    }

    private fun saveCompressedImageToFile(image: Bitmap, quality: Int): File {
        val file = File(context.getExternalFilesDir(null), "compressed_image.jpg")
        FileOutputStream(file).use { outputStream ->
            image.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }
        return file
    }
}
