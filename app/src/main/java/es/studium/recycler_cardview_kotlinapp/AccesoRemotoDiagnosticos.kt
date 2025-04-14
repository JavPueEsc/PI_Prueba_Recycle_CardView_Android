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
    val request = Request.Builder()
        .url("http://192.168.0.217/ApiRestPracticaPruebaCardView/diagnosticos.php")
        .build()
    var resultado : JSONArray = JSONArray()

    fun obtenerListado():JSONArray{

        try{
            //Realizar la solicitud
            val response = client.newCall(request).execute()
            //Procesar la respuesta
            if(response.isSuccessful){
                resultado = JSONArray(response.body?.string())
            }
            else{
                Log.e("AccesoRemoto",response.message)
            }
        }
        catch(e : IOException){
            Log.e("AccesoRemoto", e.message ?: "Error desconocido")
        }
        catch(e : JSONException){
            throw RuntimeException(e)
        }
        return resultado
    }
}