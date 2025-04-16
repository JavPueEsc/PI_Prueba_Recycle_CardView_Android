package es.studium.recycler_cardview_kotlinapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ModificarActivity : AppCompatActivity() {
    private lateinit var lbl_idModificar: TextView
    private lateinit var img_imagenModificar : ImageView
    private lateinit var lbl_fechaModificar : TextView
    private lateinit var lbl_diagnosticoModificar : TextView
    private lateinit var lbl_gravedadDiagnostico : TextView
    private lateinit var txt_doctorModificar : EditText
    private lateinit var txt_centroModificar : EditText
    private lateinit var btn_modificar : Button

    private  var idRecibido : String? = null
    private  var imagenRecibida : ByteArray? = null
    private  var fechaRecibida : String? = null
    private  var diagnosticoRecibido : String? = null
    private  var gravedadRecibida : String? = null
    private  var doctorRecibido : String? = null
    private  var centroRecibido : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar)

        lbl_idModificar = findViewById(R.id.lbl_idModificar)
        img_imagenModificar = findViewById(R.id.img_imagenModificar)
        lbl_fechaModificar = findViewById(R.id.lbl_fechaDiagModificar)
        lbl_diagnosticoModificar = findViewById(R.id.lbl_diagnosticoModificar)
        lbl_gravedadDiagnostico = findViewById(R.id.lbl_valoracionModificar)
        txt_doctorModificar = findViewById(R.id.txt_doctorModificar)
        txt_centroModificar = findViewById(R.id.txt_centroModificar)
        btn_modificar = findViewById(R.id.btn_Modificar)

        val extras = intent.extras
        if (extras != null){
             idRecibido = extras.getString("idDiagnostico")
             imagenRecibida = extras.getByteArray("imagenDiagnostico")
             fechaRecibida = extras.getString("fechaDiagnostico")
             diagnosticoRecibido = extras.getString("diagnosticoDiagnostico")
             gravedadRecibida = extras.getString("gravedadDiagnostico")
             doctorRecibido = extras.getString("doctorDiagnostico")
             centroRecibido = extras.getString("centroDiagnostico")

            lbl_idModificar.text = idRecibido
            val imagenBitmap = byteArrayABitmap(imagenRecibida)
            if (imagenBitmap != null) {
                img_imagenModificar.setImageBitmap(imagenBitmap)
            } else {
                img_imagenModificar.setImageResource(R.drawable.imagen_por_defecto)
            }
            lbl_fechaModificar.text = fechaRecibida
            lbl_diagnosticoModificar.text = diagnosticoRecibido
            lbl_gravedadDiagnostico.text = gravedadRecibida
            txt_doctorModificar.setText(doctorRecibido)
            txt_centroModificar.setText(centroRecibido)
        }

        btn_modificar.setOnClickListener {
            if (
                idRecibido != null &&
                imagenRecibida != null &&
                fechaRecibida != null &&
                diagnosticoRecibido != null &&
                gravedadRecibida != null
            ) {
                val modificacionDiagnostico = ModificacionRemotaDiagnosticos()

                // Como modificarDiagnostico es suspend, se necesita una corrutina
                CoroutineScope(Dispatchers.IO).launch {
                    val resultado = modificacionDiagnostico.modificarDiagnostico(
                        idRecibido!!,
                        imagenRecibida!!,
                        fechaRecibida!!,
                        diagnosticoRecibido!!,
                        gravedadRecibida!!,
                        txt_doctorModificar.text.toString(),
                        txt_centroModificar.text.toString()
                    )
                    //Para poder usar un toast en la rutina hay que usar este bloque
                    runOnUiThread {
                        if (resultado) {
                            Toast.makeText(this@ModificarActivity, "Diagnóstico modificado", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this@ModificarActivity, "Error al modificar el diagnóstico", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Log.e("MODIFICACION", "Alguno de los valores es null. No se puede modificar.")
            }

            //Volver al Activity Principal tras la modificacion
            var intent = Intent(this,MainActivity::class.java)
            intent.putExtra("Recargar",true)
            startActivity(intent)
            finish()
        }


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