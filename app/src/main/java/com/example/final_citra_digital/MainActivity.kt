package com.example.final_citra_digital

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {

    private lateinit var imageClassifier: ImageClassifier
    private lateinit var imageView: ImageView
    private lateinit var textViewResult: TextView

    // Tentukan ambang batas untuk menentukan validitas prediksi
    private val THRESHOLD = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageClassifier = ImageClassifier(assets)
        imageView = findViewById(R.id.imageView)
        textViewResult = findViewById(R.id.textView_result)

        val buttonSelectImage: Button = findViewById(R.id.button_select_image)
        val buttonCaptureImage: Button = findViewById(R.id.button_capture_image)

        // Tombol untuk memilih gambar dari galeri
        buttonSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // Tombol untuk menangkap gambar dari kamera
        buttonCaptureImage.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    val uri = data?.data
                    val inputStream = contentResolver.openInputStream(uri!!)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageView.setImageBitmap(bitmap)
                    classifyImage(bitmap)
                }
                REQUEST_IMAGE_CAPTURE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    imageView.setImageBitmap(bitmap)
                    classifyImage(bitmap)
                }
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap) {
        // Mengubah ukuran gambar dan mengonversi ke ByteBuffer
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val inputBuffer = convertBitmapToByteBuffer(resizedBitmap)

        // Mendapatkan hasil prediksi dan skor
        val (result, score) = imageClassifier.classifyImage(inputBuffer)

        // Cek apakah skor di atas ambang batas, jika tidak tampilkan Not Found
        textViewResult.text = if (score >= THRESHOLD) {
            "Hasil Prediksi: $result\nAkurasi: ${(score * 100).toInt()}%"
        } else {
            "Hasil Prediksi: Tidak Ada Hasil, Score Kurang Dari Akurasi\nAkurasi: ${(score * 100).toInt()}%"
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val inputBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        inputBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(224 * 224)
        bitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        intValues.forEach { pixelValue ->
            inputBuffer.putFloat(((pixelValue shr 16) and 0xFF) / 255.0f)
            inputBuffer.putFloat(((pixelValue shr 8) and 0xFF) / 255.0f)
            inputBuffer.putFloat((pixelValue and 0xFF) / 255.0f)
        }
        return inputBuffer
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
        private const val REQUEST_IMAGE_CAPTURE = 2
    }
}
