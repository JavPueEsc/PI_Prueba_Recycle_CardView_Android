package es.studium.recycler_cardview_kotlinapp


import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import android.util.Base64

class AltaRemotaDiagnosticos {
    val client = OkHttpClient()

    suspend fun darAltaDiagnosticoEnBD(
        imagenBytes: ByteArray,
        fecha: String,
        diagnostico: String,
        gravedad: String,
        doctor: String,
        centro: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val imagenBase64 = Base64.encodeToString(imagenBytes, Base64.DEFAULT)

                val formBody = FormBody.Builder()
                    .add("imagenDiagnostico", imagenBase64)
                    .add("fechaDiagnostico", fecha)
                    .add("diagnosticoDiagnostico", diagnostico)
                    .add("gravedadDiagnostico", gravedad)
                    .add("doctorDiagnostico", doctor)
                    .add("centroDiagnostico", centro)
                    .build()

                val request = Request.Builder()
                    .url("http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php")
                    .post(formBody)
                    .build()

                val response = client.newCall(request).execute()
                Log.d("RESPUESTA", response.body?.string() ?: "Sin cuerpo")
                response.isSuccessful
            } catch (e: IOException) {
                Log.e("ErrorInsercion", "Error al insertar diagnóstico: ${e.message}")
                false
            }
        }
    }
}
