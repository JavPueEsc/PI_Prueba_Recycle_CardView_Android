package es.studium.recycler_cardview_kotlinapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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

class AltaActivity : AppCompatActivity() {
    private lateinit var fechaDiag : TextView
    private lateinit var imagenDiag : ImageView
    private lateinit var diagnosticoDiag : TextView
    private lateinit var valoracionDiag : TextView
    private lateinit var txtDoctorDiag : EditText
    private lateinit var txtCentroDiag : EditText
    private lateinit var btnAltaInsercion : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alta)

        val extras = intent.extras
        if(extras != null){
            val imagenRecibida = extras.getString("imagenDiagnostico")
            val fechaRecibida = extras.getString("fechaDiagnostico")
            val diagnosticoRecibido = extras.getString("diagnosticoDiagnostico")

            fechaDiag = findViewById(R.id.lbl_fechaDiagAlta)
            imagenDiag = findViewById(R.id.img_imagenAlta)
            diagnosticoDiag = findViewById(R.id.lbl_diagnosticoAlta)
            valoracionDiag = findViewById(R.id.lbl_valoracionAlta)
            txtDoctorDiag = findViewById(R.id.txt_doctorAlta)
            txtCentroDiag = findViewById(R.id.txt_centroAlta)
            btnAltaInsercion =findViewById(R.id.btn_insercion)

            fechaDiag.text = fechaRecibida
            diagnosticoDiag.text = diagnosticoRecibido

            //Comprobamos que la Uri recibida no esta vacía y como llega como
            //String, la transformamos a dato tipo URI
            if (imagenRecibida != null) {
                establecerImagenDesdeUri(Uri.parse(imagenRecibida))
            } else {
                // Maneja el caso en que no se recibe la imagen
                Toast.makeText(this, "Imagen no disponible", Toast.LENGTH_SHORT).show()
            }
            var diagnosticoAEnviar =""
            if(diagnosticoRecibido != null){
                diagnosticoAEnviar = diagnosticoRecibido
            }
            //Establecemos la valoración preliminar
            //Establecemos valoración
            lateinit var valoracion :String
            if(diagnosticoAEnviar.trim().equals("Melanoma", ignoreCase = true)){
                valoracion = "Maligno"
                valoracionDiag.setText(valoracion)
            }
            else{
                valoracion = "Benigno"
                valoracionDiag.setText(valoracion)
            }
            btnAltaInsercion.setOnClickListener {

                var fechaAEnviar =""
                if(fechaRecibida != null){
                    fechaAEnviar = fechaRecibida
                }

                var doctorAEnviar = "Vacio"
                if(!txtDoctorDiag.text.isEmpty()){
                    doctorAEnviar = txtDoctorDiag.text.toString()
                }

                var centroAEnviar = "Vacio"
                if(!txtCentroDiag.text.isEmpty()){
                    centroAEnviar = txtCentroDiag.text.toString()
                }

                //Insercion en base de datos
                val altaDiagnostico = AltaRemotaDiagnosticos()
                //pasamos la imagen de Uri a ByteArray
                var imagenByteArray = uriAByteArray(Uri.parse(imagenRecibida))

                CoroutineScope(Dispatchers.Main).launch {
                    //Inserción
                    var verificarAlta: Boolean = altaDiagnostico.darAltaDiagnosticoEnBD(
                        imagenByteArray, fechaAEnviar, diagnosticoAEnviar,
                        valoracion, doctorAEnviar, centroAEnviar
                    )

                    // Loguear los datos antes de la inserción
                    Log.d(
                        "Datos enviados",
                        "Imagen: ${imagenByteArray.size} bytes, Fecha: $fechaAEnviar, Diagnóstico: $diagnosticoAEnviar, Gravedad: $valoracion, Doctor: $doctorAEnviar, Centro: $centroAEnviar"
                    )

                    if (verificarAlta) {
                        Toast.makeText(this@AltaActivity, "ALTA CORRECTA", Toast.LENGTH_SHORT).show()
                        var intent = Intent(this@AltaActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@AltaActivity, "ERROR ALTA", Toast.LENGTH_SHORT).show()
                        // Log de error de inserción
                        Log.e(
                            "ErrorInsercion",
                            "Fallo al insertar los datos del diagnóstico. Revisa los valores y la conexión."
                        )
                    }

                }

            }

        }

    }

    // Metodo para establecer la imagen en el ImageView a partir de una URI
    private fun establecerImagenDesdeUri(uri: Uri) {
        imagenDiag.setImageURI(uri)
    }

    //Metodo para transformar de URI a ByteArray y así poder pasarselo al
    // meto que inserta en la base de datos
    fun uriAByteArray(uri: Uri): ByteArray {
        val inputStream = contentResolver.openInputStream(uri)
        return inputStream?.readBytes() ?: ByteArray(0)
    }
}