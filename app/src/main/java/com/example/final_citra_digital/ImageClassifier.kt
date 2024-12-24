package com.example.final_citra_digital

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ImageClassifier(assetManager: AssetManager) {

    private val interpreter: Interpreter
    private val labelList: List<String>

    init {
        // Memuat model TensorFlow Lite dan daftar label
        interpreter = Interpreter(loadModelFile(assetManager, "model.tflite"))
        labelList = loadLabelList(assetManager, "labels.txt")
    }

    // Memuat file model TFLite dari assets
    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor: AssetFileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Memuat daftar label dari file labels.txt di assets
    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String> {
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

    // Melakukan klasifikasi gambar dan mengembalikan label dengan skor prediksi tertinggi
    fun classifyImage(inputBuffer: ByteBuffer): Pair<String, Float> {
        // Array untuk hasil prediksi
        val result = Array(1) { FloatArray(labelList.size) }
        interpreter.run(inputBuffer, result)

        // Mendapatkan indeks skor tertinggi
        val maxIndex = result[0].indices.maxByOrNull { result[0][it] } ?: -1
        val maxScore = result[0][maxIndex]

        // Menentukan label berdasarkan indeks skor tertinggi atau menampilkan Not Found jika tidak ada
        val label = if (maxIndex != -1 && maxIndex < labelList.size) labelList[maxIndex] else "Not Found"
        return Pair(label, maxScore)
    }
}
