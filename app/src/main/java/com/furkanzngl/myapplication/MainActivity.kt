package com.furkanzngl.myapplication

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ImageViewModel
    private lateinit var imageView: ImageView
    private lateinit var compressedImageView: ImageView
    private lateinit var downloadButton: Button
    private lateinit var chosenImageBitmap: Bitmap
    private var savedFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // UI bileşenlerini tanımla
        imageView = findViewById(R.id.imageView)
        compressedImageView = findViewById(R.id.compressedImageView)
        downloadButton = findViewById(R.id.downloadButton)
        val chooseImageButton: Button = findViewById(R.id.chooseImageButton)
        val compressImageButton: Button = findViewById(R.id.compressImageButton)
        val customCompressButton: Button = findViewById(R.id.customCompressButton)

        // Repository ve ViewModel oluştur
        val imageRepository = ImageRepository(ImageCompressor())
        viewModel = ImageViewModel(imageRepository, this)

        // Sıkıştırılmış görüntüyü gözlemle ve ImageView'da göster
        viewModel.compressedImage.observe(this, Observer { compressedImage ->
            compressedImageView.setImageBitmap(compressedImage)
            compressedImageView.visibility = View.VISIBLE
            downloadButton.visibility = View.VISIBLE
        })

        // Choose Image butonuna tıklama işlemi
        chooseImageButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_CHOOSE_IMAGE)
        }

        // Compress Image butonuna tıklama işlemi
        compressImageButton.setOnClickListener {
            viewModel.compressImage(chosenImageBitmap, 100) // %80 kalite ile sıkıştır
        }

        // Custom Compress butonuna tıklama işlemi
        customCompressButton.setOnClickListener {
            showCustomCompressDialog()
        }

        // Download butonuna tıklama işlemi
        downloadButton.setOnClickListener {
            savedFile?.let {
                val uri = FileProvider.getUriForFile(this, "${packageName}.provider", it)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "image/*")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                startActivity(intent)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            val uri = data?.data
            uri?.let {
                chosenImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                imageView.setImageBitmap(chosenImageBitmap)
                findViewById<Button>(R.id.compressImageButton).isEnabled = true
                findViewById<Button>(R.id.customCompressButton).isEnabled = true
            }
        }
    }

    private fun showCustomCompressDialog() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_custom_compress, null)
        val qualityEditText = dialogLayout.findViewById<EditText>(R.id.customQualityEditText)
        val widthEditText = dialogLayout.findViewById<EditText>(R.id.widthEditText)
        val heightEditText = dialogLayout.findViewById<EditText>(R.id.heightEditText)

        with(builder) {
            setTitle("Custom Compression")
            setView(dialogLayout)
            setPositiveButton("OK") { dialog, _ ->
                val quality = qualityEditText.text.toString().toIntOrNull()
                val width = widthEditText.text.toString().toIntOrNull()
                val height = heightEditText.text.toString().toIntOrNull()

                if (quality != null && width != null && height != null && quality in 1..100) {
                    savedFile = viewModel.compressAndResizeAndSaveImage(chosenImageBitmap, quality, width, height)
                    savedFile?.let {
                        Toast.makeText(this@MainActivity, "Resim şuraya kaydedildi: ${it.path}", Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(this@MainActivity, "Sıkıştırma başarısız", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "LÜtfen 0 ila 100 arasında bir yükseklik ve genişlik giriniz", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            setNegativeButton("Pas geç") { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    companion object {
        const val REQUEST_CODE_CHOOSE_IMAGE = 1001
    }
}
