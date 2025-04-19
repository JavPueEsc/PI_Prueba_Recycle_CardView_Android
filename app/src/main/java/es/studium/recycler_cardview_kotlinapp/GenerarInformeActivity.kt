package es.studium.recycler_cardview_kotlinapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class GenerarInformeActivity : AppCompatActivity() {
    //variables para acceder a los valores del diagnostico en la BD
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

    //Botón generar informe
    private lateinit var btn_generarInforme : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_informe)

        btn_generarInforme = findViewById(R.id.btn_generarInforme)

        val extras = intent.extras
        if(extras != null){
            val idRecibido = extras.getString("idDiagnostico")

            if (idRecibido != null) {
                cargarDatosDelDiagnosticoSeleccionado(idRecibido)
            } else {
                Toast.makeText(this, "ID del diagnóstico no recibido", Toast.LENGTH_SHORT).show()
            }
        }

        var lbl_fechaDiag : TextView= findViewById(R.id.lbl_valorFechaDiasg1)
        var img_diag : ImageView = findViewById(R.id.img_imagenInforme)
        var lbl_diagnostico : TextView = findViewById(R.id.lbl_valorDiagnostico1)
        var lbl_valoracion : TextView = findViewById(R.id.lbl_valorValoracion)
        var lbl_doctor : TextView = findViewById(R.id.lbl_valorDoctor1)
        var lbl_centro : TextView = findViewById(R.id.lbl_valorCentro)

        lbl_fechaDiag.text = fechaDiagnosticoBD
        val imagenBitmap = byteArrayABitmap(imagenDiagnosticoBD)
        if (imagenBitmap != null) {
            img_diag.setImageBitmap(imagenBitmap)
        } else {
            img_diag.setImageResource(R.drawable.imagen_por_defecto)
        }
        lbl_diagnostico.text=diagnosticoDiagnosticoBD
        lbl_valoracion.text = gravedadDiagnosticoBD
        lbl_doctor.text = doctorDiagnosticoBD
        lbl_centro.text = centroDiagnosticoBD

        btn_generarInforme.setOnClickListener {
            //Implementar la lógica de la creacion del pdf
        }

    }

    fun cargarDatosDelDiagnosticoSeleccionado(idDiagnostico : String) {
        //-----Cominucacion con la API-Rest-----------
        if (android.os.Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        var accesoRemotoDiagnosticos = AccesoRemotoDiagnosticos()
        result = accesoRemotoDiagnosticos.obtenerDiagnosticoPorId(idDiagnostico)
        //verificamos que result no está vacio
        try {
            if (result.length() > 0) {
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

                    //listaDiagnosticos.add(ModeloDiagnostico(idDiagnosticoBD,imagenDiagnosticoBD,fechaDiagnosticoBD,diagnosticoDiagnosticoBD,
                    //gravedadDiagnosticoBD,doctorDiagnosticoBD,centroDiagnosticoBD))
                }
            } else {
                Log.e("MainActivity", "El JSONObject está vacío")
            }
        } catch (e: JSONException) {
            Log.e("MainActivity", "Error al procesar el JSON", e)
        }
    }

    //Metodo para pasar de StringBase64 a ByteArray
    fun base64AByteArray(base64String: String): ByteArray {
        return Base64.decode(base64String, Base64.DEFAULT)
    }

    fun byteArrayABitmap(byteArray: ByteArray?): Bitmap? {
        return if (byteArray != null) {
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            if (bitmap != null) {
                Log.d("CONVERSION_IMAGEN", "Conversión a Bitmap exitosa. Tamaño: ${byteArray.size} bytes")
            } else {
                Log.e("CONVERSION_IMAGEN", "Fallo en la conversión a Bitmap. ByteArray no válido.")
            }
            bitmap
        } else {
            Log.w("CONVERSION_IMAGEN", "ByteArray es null. No se puede convertir.")
            null
        }
    }
}