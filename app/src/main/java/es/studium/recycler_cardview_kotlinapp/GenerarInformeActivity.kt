package es.studium.recycler_cardview_kotlinapp

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.text.TextPaint
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

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

    //Variables de texto para incluir en pdf
    //Generar pdf con pdfDocument
    private lateinit var tituloInforme : String
    private lateinit var subtituloIdentificacionPaciente : String
    private lateinit var nuhsa : String
    private lateinit var paciente : String
    private lateinit var fechaEdadSexo : String
    private lateinit var localidadCP : String
    private lateinit var subtituloDiagnóstico : String
    private lateinit var imagenYFecha : String
    private lateinit var diagnosticoVisual : String
    private lateinit var valoracion : String
    private lateinit var doctor : String
    private lateinit var especialidad : String
    private lateinit var centro : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_informe)

        /*pedir los permisos al usuariopara generar el pdf
         if(comprobarPermisos()){
            Toast.makeText(this,"Permiso Aceptado", Toast.LENGTH_SHORT).show()
        }
        else{
            pedirPermisos()
        }*/

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

            val uri =crearYGuardarPDFDesdeBytes("Informe_diagnostico_$idDiagnosticoBD.pdf", imagenDiagnosticoBD)

            //Abir directamente el informe tras crearlo
            if (uri != null) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NO_HISTORY
                }
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No se encontró una aplicación para abrir PDF", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun crearYGuardarPDFDesdeBytes(nombreArchivo: String, imagenBytes: ByteArray) : Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Estilos de texto
        val paintTitulo = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = Color.BLACK
        }

        val paintSubTitulo = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
            textSize = 20f
            color = Color.BLACK
        }

        val paintTextoNormal = Paint().apply {
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textSize = 14f
            color = Color.DKGRAY
        }


        // Dibujar texto
        canvas.drawText(tituloInforme, 40f, 40f, paintTitulo)
        canvas.drawText(subtituloIdentificacionPaciente, 40f, 80f, paintSubTitulo)
        canvas.drawText(nuhsa, 40f, 120f, paintTextoNormal)
        canvas.drawText(paciente, 40f, 160f, paintTextoNormal)
        canvas.drawText(fechaEdadSexo, 40f, 200f, paintTextoNormal)
        canvas.drawText(localidadCP, 40f, 240f, paintTextoNormal)
        canvas.drawText(subtituloDiagnóstico, 40f, 280f, paintSubTitulo)
        canvas.drawText(imagenYFecha, 40f, 320f, paintTextoNormal)

        // Convertir ByteArray a Bitmap e insertar
        val bitmap = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.size)
        val resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true)
        canvas.drawBitmap(resized, 40f, 360f, null)

        canvas.drawText(diagnosticoVisual, 40f, 624f, paintTextoNormal)
        canvas.drawText(valoracion, 40f, 664f, paintTextoNormal)
        canvas.drawText(doctor, 40f, 704f, paintTextoNormal)
        canvas.drawText(especialidad, 40f, 744f, paintTextoNormal)
        canvas.drawText(centro, 40f, 784f, paintTextoNormal)

        pdfDocument.finishPage(page)

        // Guardar usando MediaStore (a partir de Android 10)
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, nombreArchivo)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = resolver.openOutputStream(uri)
                pdfDocument.writeTo(outputStream!!)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
                Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error al guardar PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } finally {
                outputStream?.close()
                pdfDocument.close()
            }
        } else {
            Toast.makeText(this, "No se pudo crear el archivo PDF", Toast.LENGTH_SHORT).show()
            pdfDocument.close()
        }
        return uri
    }


    private fun cargarDatosDelDiagnosticoSeleccionado(idDiagnostico : String) {
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

                    //Iniciamos texto para el pdf
                     tituloInforme = "Diagnóstico dermatológico visual"
                     subtituloIdentificacionPaciente = "Identificación del paciente"
                     nuhsa = "NUSHA: AN0073847313"
                     paciente = "Paciente: Agustín Acosta Jaramillo"
                     fechaEdadSexo = "Fecha Nac.: 09/10/1946    Edad: 79    Sexo: Hombre"
                     localidadCP = "Localidad: Jerez de la Frontera (Cádiz)    C.P.:11407"
                     subtituloDiagnóstico = "Datos del diagnóstico"
                     imagenYFecha = "Imágen de la lesión:    Fecha del diagnóstico: ${fechaMysqlAEuropea(fechaDiagnosticoBD)}"
                     diagnosticoVisual = "Diagnóstico visual: $diagnosticoDiagnosticoBD"
                     valoracion = "Valoracion: $gravedadDiagnosticoBD"
                     doctor ="Doctor/a: $doctorDiagnosticoBD"
                     especialidad ="Especialidad: Médico de cabecera"
                     centro = "Centro de diagnóstico: $centroDiagnosticoBD"

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
    //Metodo para pasar fechas MySQL a Europeo
    fun fechaMysqlAEuropea(fecha: String): String {
        lateinit var fechaTransformada: String
        var elementosFecha = fecha.split("-")
        if (elementosFecha.size == 3) {
            fechaTransformada = "${elementosFecha[2]}/${elementosFecha[1]}/${elementosFecha[0]}"
        } else {
            fechaTransformada = "Error al formatear fechas"
        }
        return fechaTransformada
    }
}