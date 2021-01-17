package com.example.tflite_fashion_mnist

import android.content.res.AssetFileDescriptor
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.Comparator
import kotlin.math.min

class Classifier(assetManager: AssetManager, modelPath: String, labelPath: String, private val inputSize: Int) {

    private var interpreter: Interpreter
    private var labelList: List<String>
    private val pixelSize: Int = 1
    private val imageMean = 0
    private val imageStd = 255.0f
    private val maxResult = 3
    private val threshHold = 0.5f

    data class Recognition(
        var id: String = "",
        var title: String = "",
        var confidence: Float = 0F
    )  {
        override fun toString(): String {
            return "Title = $title, Confidence = $confidence)"
        }
    }

    init {
        val options = Interpreter.Options()
        options.setNumThreads(5)
        options.setUseNNAPI(true)
        interpreter = loadModelFile(assetManager, modelPath)?.let { Interpreter(it, options) }!!
        labelList = loadLabelList(assetManager, labelPath)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer? {
        val fileDescriptor : AssetFileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel : FileChannel = inputStream.channel
        val startOffSet = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffSet, declaredLength)
    }

    private fun loadLabelList(assetManager: AssetManager, labelPath: String): List<String>{
        return assetManager.open(labelPath).bufferedReader().useLines { it.toList() }
    }

//    private fun resizeBitmap(bitmap: Bitmap): Bitmap? {
//        return Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
//    }

    fun recognizeImage(bitmap: Bitmap): List<Recognition> {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, false)
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val result = Array(1) { FloatArray(labelList.size) }
        Log.d("BYTEBUFFER INFO", byteBuffer.toString())
        interpreter.run(byteBuffer, result)
        return getSortedResult(result)
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer{
        val byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * pixelSize)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(inputSize * inputSize)

        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until inputSize) {
            for (j in 0 until inputSize) {
                val input = intValues[pixel++]
                byteBuffer.putFloat((((input and 0xFF) - imageMean) / imageStd))
            }
        }
        return byteBuffer
    }

    private fun getSortedResult(labelProbArray: Array<FloatArray>): List<Recognition> {
        Log.d("Classifier",
            "List Size:(%d, %d, %d)".format(labelProbArray.size,labelProbArray[0].size,labelList.size))

        val pq = PriorityQueue(
            maxResult,
            Comparator<Recognition> {
                    (_, _, confidence1), (_, _, confidence2)
                -> confidence1.compareTo(confidence2) * -1
            })

        for (i in labelList.indices) {
            val confidence = labelProbArray[0][i]
            Log.d("RECOG INFO: Confidence", confidence.toString())
            if (confidence >= threshHold) {
                Log.d("RECOG INFO: Confidence2", confidence.toString())
                pq.add(Recognition("" + i,
                    if (labelList.size > i) labelList[i] else "Unknown", confidence
                )
                )
            }
        }
        Log.d("Classifier", "pqsize:(%d)".format(pq.size))
        Log.d("RECOG INFO", pq.element().toString())

        val recognitions = ArrayList<Recognition>()
        val recognitionsSize = min(pq.size, maxResult)
        for (i in 0 until recognitionsSize) {
            recognitions.add(pq.poll())
        }
        return recognitions
    }
}