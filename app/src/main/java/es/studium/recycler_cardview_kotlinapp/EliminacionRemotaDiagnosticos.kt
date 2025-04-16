package es.studium.recycler_cardview_kotlinapp

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException

class EliminacionRemotaDiagnosticos {

    val client = OkHttpClient()
    private var correcta: Boolean = true
    fun eliminarDiagnostico(id: String): Boolean {

        var request = Request.Builder()
            .url("http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php?idDiagnostico=$id")
            .delete()
            .build()
        var call = client.newCall(request)
        try {
            var response = call.execute()
            Log.i("BajaRemota", response.toString())
            correcta = true
            return correcta
        } catch (e: IOException) {
            Log.e("BajeRemota", e.message.toString())
            correcta = false
            return correcta
        }
    }
}