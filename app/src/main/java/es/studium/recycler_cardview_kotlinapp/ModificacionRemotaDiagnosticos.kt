package es.studium.recycler_cardview_kotlinapp


import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class ModificacionRemotaDiagnosticos {
    private  var client : OkHttpClient = OkHttpClient()

    suspend fun modificarDiagnostico(
        id : String,
        imagenBytes: ByteArray,
        fecha: String,
        diagnostico: String,
        gravedad: String,
        doctor: String,
        centro: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Convertir la imagen en base64 (si es necesario)
                val imagenBase64 = Base64.encodeToString(imagenBytes, Base64.DEFAULT)

                // Construir el cuerpo de la solicitud con los parámetros
                val formBody = FormBody.Builder()
                    .add("idDiagnostico", id)
                    .add("imagenDiagnostico", imagenBase64)  // Enviar imagen como base64
                    .add("fechaDiagnostico", fecha)
                    .add("diagnosticoDiagnostico", diagnostico)
                    .add("gravedadDiagnostico", gravedad)
                    .add("doctorDiagnostico", doctor)
                    .add("centroDiagnostico", centro)
                    .build()

                // Crear la solicitud PUT
                val request = Request.Builder()
                    .url("http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php")
                    .put(formBody)
                    .build()

                // Realizar la petición y obtener la respuesta
                val response = client.newCall(request).execute()
                Log.d("ModificacionRemota", response.body?.string() ?: "Sin cuerpo")

                // Verificar si la respuesta fue exitosa
                response.isSuccessful
            } catch (e: IOException) {
                Log.e("ModificacionRemota", "Error al modificar diagnóstico: ${e.message}")
                false
            }
        }
    }

}