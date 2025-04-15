package es.studium.recycler_cardview_kotlinapp

import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import android.util.Base64
import android.util.Log
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONException

class MainActivity : AppCompatActivity() {
    private lateinit var btnAlta : Button
    //Variables para la base de datos
    private var listaDiagnosticos: MutableList<ModeloDiagnostico> = mutableListOf()
    val adaptadorDiagnosticos = AdaptadorDiagnosticos(listaDiagnosticos)

    private lateinit var recyclerView: RecyclerView
    private lateinit var result : JSONArray
    private lateinit var jsonObject: JSONObject
    private lateinit var idDiagnosticoBD : String
    private lateinit var imagenDiagnosticoBD_stringBase64 : String
    private var imagenDiagnosticoBD = byteArrayOf()
    private lateinit var fechaDiagnosticoBD : String
    private lateinit var diagnosticoDiagnosticoBD : String
    private lateinit var gravedadDiagnosticoBD : String
    private lateinit var doctorDiagnosticoBD : String
    private lateinit var centroDiagnosticoBD : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAlta=findViewById(R.id.btnAlta)

        //-----Cominucacion con la API-Rest-----------
        if(android.os.Build.VERSION.SDK_INT > 9){
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        var accesoRemotoDiagnosticos = AccesoRemotoDiagnosticos()
        result = accesoRemotoDiagnosticos.obtenerListado()
        //verificamos que result no está vacio
        try{
            if(result.length() > 0){
                for (i in 0 until result.length()) {
                    jsonObject = result.getJSONObject(i)
                    idDiagnosticoBD = jsonObject.getString("idDiagnostico")
                    imagenDiagnosticoBD_stringBase64 = jsonObject.getString("imagenDiagnostico")
                    //pasar el string de imagen a bitArray
                    imagenDiagnosticoBD = base64AByteArray(imagenDiagnosticoBD_stringBase64)
                    fechaDiagnosticoBD = jsonObject.getString("fechaDiagnostico")
                    diagnosticoDiagnosticoBD = jsonObject.getString("diagnosticoDiagnostico")
                    gravedadDiagnosticoBD = jsonObject.getString("gravedadDiagnostico")
                    doctorDiagnosticoBD = jsonObject.getString("doctorDiagnostico")
                    centroDiagnosticoBD = jsonObject.getString("centroDiagnostico")

                   listaDiagnosticos.add(ModeloDiagnostico(idDiagnosticoBD,imagenDiagnosticoBD,fechaDiagnosticoBD,diagnosticoDiagnosticoBD,
                       gravedadDiagnosticoBD,doctorDiagnosticoBD,centroDiagnosticoBD))
                }
            }
            else{
                Log.e("MainActivity", "El JSONObject está vacío")
            }
        }
        catch(e : JSONException){
            Log.e("MainActivity", "Error al procesar el JSON", e)
        }

        //Ponemos la lista al adaptador y configuramos el recyclerView
        val mLayoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.recyclerViewDiagnosticos)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.adapter = adaptadorDiagnosticos


        //Gestión del botón para pasar a la siguiente Activity
        btnAlta.setOnClickListener {
            var intent = Intent(this,prediccionActivity::class.java)
            startActivity(intent)
        }
    }

    //Metodo para pasar de StringBase64 a ByteArray
    fun base64AByteArray(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }
}