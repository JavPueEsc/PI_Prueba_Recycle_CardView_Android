package es.studium.recycler_cardview_kotlinapp

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder



class ModelPredictor(private val context: Context) {
    private lateinit var tflite: org.tensorflow.lite.Interpreter

    init {
        // Cargar el modelo .tflite
        val model = FileUtil.loadMappedFile(context, "modelo_optimizado.tflite")
        tflite = org.tensorflow.lite.Interpreter(model)
    }

    fun predict(imageUri: Uri): Pair<String, Float> {
        // Cargar y preprocesar la imagen usando processImageForPrediction
        val byteBuffer = processImageForPrediction(imageUri)

        // Crear el tensor de salida
        val output = TensorBuffer.createFixedSize(intArrayOf(1, NUM_CLASSES), DataType.FLOAT32)

        // Ejecutar la predicción
        tflite.run(byteBuffer, output.buffer.rewind())

        // Obtener el índice de la clase con mayor probabilidad
        val predictedIndex = getPredictedIndex(output)
        val confidence = output.floatArray[predictedIndex] * 100f // Multiplica por 100 para porcentaje

        // Devuelve la clase predicha y su precisión
        return Pair(getClassName(predictedIndex), confidence)
    }

    private fun processImageForPrediction(imageUri: Uri): ByteBuffer {
        // Obtener el Bitmap de la imagen desde la URI
        val bitmap = getBitmapFromUri(imageUri)

        // Redimensionar la imagen a 224x224
        val resizedImage = Bitmap.createScaledBitmap(bitmap, 224, 224, false)

        // Crear un ByteBuffer para almacenar los valores RGB
        val byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Convertir la imagen a un array de bytes (RGB)
        val pixels = IntArray(224 * 224)
        resizedImage.getPixels(pixels, 0, 224, 0, 0, 224, 224)

        // Pasa los valores RGB al buffer
        for (pixel in pixels) {
            val r = ((pixel shr 16) and 0xFF) / 255.0f
            val g = ((pixel shr 8) and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap {
        // Obtener el Bitmap a partir de la Uri
        return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    private fun getPredictedIndex(output: TensorBuffer): Int {
        val outputArray = output.floatArray
        // Obtener el índice de la clase con la mayor probabilidad
        val max = outputArray.maxOrNull() ?: 0f
        return outputArray.indexOfFirst { it == max }
    }

    private fun getClassName(predictedIndex: Int): String {
        // Aquí deberías tener un listado de las clases para devolver el nombre adecuado
        val classNames = listOf("Melanoma", "Nevus", "Angioma", "Dermatofibroma", "Onicomicosis") // Ejemplo de clases
        return classNames.getOrElse(predictedIndex) { "Desconocido" } // Retorna "Desconocido" si el índice está fuera de rango
    }

    companion object {
        private const val NUM_CLASSES = 5 // Número de clases que tiene el modelo
    }
}