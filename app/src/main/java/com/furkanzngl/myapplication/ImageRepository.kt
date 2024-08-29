package com.furkanzngl.myapplication

import android.graphics.Bitmap

class ImageRepository(private val imageCompressor: ImageCompressor) {

    fun compressImage(image: Bitmap, quality: Int): Bitmap {
        return imageCompressor.compress(image, quality)
    }

    fun compressAndResizeImage(image: Bitmap, quality: Int, width: Int, height: Int): Bitmap {
        return imageCompressor.compressAndResize(image, quality, width, height)
    }
}
