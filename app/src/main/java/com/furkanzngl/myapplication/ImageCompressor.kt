package com.furkanzngl.myapplication

import android.graphics.Bitmap

class ImageCompressor {

    fun compress(image: Bitmap, quality: Int): Bitmap {
        // Sıkıştırma işlemi
        return image
    }

    fun compressAndResize(image: Bitmap, quality: Int, width: Int, height: Int): Bitmap {
        val resizedImage = Bitmap.createScaledBitmap(image, width, height, true)
        return compress(resizedImage, quality)
    }
}

