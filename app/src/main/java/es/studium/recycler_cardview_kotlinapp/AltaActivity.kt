package es.studium.recycler_cardview_kotlinapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AltaActivity : AppCompatActivity() {
    private lateinit var fechaDiag : TextView
    private lateinit var imagenDiag : ImageView
    private lateinit var diagnosticoDiag : TextView
    private lateinit var valoracionDiag : TextView
    private lateinit var txtDoctorDiag : EditText
    private lateinit var txtCentroDiag : EditText
    private lateinit var btnAlta : Button

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

            fechaDiag.setText(fechaRecibida)
            diagnosticoDiag.setText(diagnosticoRecibido)
        }



    }
}