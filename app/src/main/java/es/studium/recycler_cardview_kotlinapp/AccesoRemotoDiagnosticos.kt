package es.studium.recycler_cardview_kotlinapp

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONArray
import org.json.JSONException

class AccesoRemotoDiagnosticos {
    //Crear una instancia de okHttpClient
    val client = OkHttpClient()
    var resultado : JSONArray = JSONArray()

    //Obtener todos los diagnosticos
    fun obtenerListado():JSONArray{
        val request = Request.Builder()
            .url("http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php")
            .build()
        return ejecutarPeticion(request)
    }

    // Obtener los diagnosticos entre dos fechas
    fun obtenerListadoEntreFechas(fechaDesde: String, fechaHasta: String): JSONArray {
        val url = "http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php?fechaDesde=$fechaDesde&fechaHasta=$fechaHasta"
        val request = Request.Builder()
            .url(url)
            .build()
        return ejecutarPeticion(request)
    }

    //Obtener diagnóstico por ID
    fun obtenerDiagnosticoPorId(idDiagnostico: String): JSONArray {
        val url = "http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php?idDiagnostico=$idDiagnostico"
        val request = Request.Builder()
            .url(url)
            .build()
        return ejecutarPeticion(request)
    }

    // Metodo ejecutar la solicitud
    private fun ejecutarPeticion(request: Request): JSONArray {
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                resultado = JSONArray(response.body?.string())
            } else {
                Log.e("AccesoRemoto", response.message)
            }
        } catch (e: IOException) {
            Log.e("AccesoRemoto", e.message ?: "Error desconocido")
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        return resultado
    }
}