package es.studium.recycler_cardview_kotlinapp

import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

class SeleccionarInformeActivity : AppCompatActivity() {
    private lateinit var txt_fechaDesde: EditText
    private lateinit var txt_fechaHasta: EditText
    private lateinit var btn_buscarDiadnostico: Button
    private lateinit var spinner_diagnosticos: Spinner
    private lateinit var lbl_valorId: TextView
    private lateinit var lbl_valorFecha: TextView
    private lateinit var lbl_valorDiagnostico: TextView
    private lateinit var btn_previsualizarInforme : Button

    //variables para la consulta a la bbdd
    private lateinit var result: JSONArray
    private lateinit var jsonObject: JSONObject
    private lateinit var idDiagnosticoBD: String
    private lateinit var fechaDiagnosticoBD: String
    private lateinit var diagnosticoDiagnosticoBD: String


    //Variable para acumular los resultados de diagnosticos para montar el spinner
    private var listaDiagnosticosSpinner: MutableList<String> = mutableListOf()
    private lateinit var adaptadorSpinner: ArrayAdapter<String>

    //variables para acceder a la posición del spinner seleccionada
    private lateinit var idSeleccionado : String
    private lateinit var  fechaSeleccionada : String
    private lateinit var  diagnosticoSeleccionado : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_informe)

        txt_fechaDesde = findViewById(R.id.txt_fechaDesde)
        txt_fechaHasta = findViewById(R.id.txt_fechaHasta)
        btn_buscarDiadnostico = findViewById(R.id.btn_buscarDiagnosticos)
        spinner_diagnosticos = findViewById(R.id.spinner_diagnosticos)
        spinner_diagnosticos.visibility =
            View.GONE // <--El Spinner debe estar oculto al inicio de la Activity
        lbl_valorId = findViewById(R.id.lblValor_idPreVisualInforme)
        lbl_valorFecha = findViewById(R.id.lblValor_fechaPreVisualInforme)
        lbl_valorDiagnostico = findViewById(R.id.lblValor_diagnosticoPreVisualInforme)
        btn_previsualizarInforme = findViewById(R.id.btn_previsualizarInforme)

        //Asignando esta propiedad, evitamos que salga el
        //teclado cuando presionamos sobre los EditText
        txt_fechaDesde.showSoftInputOnFocus = false
        txt_fechaHasta.showSoftInputOnFocus = false

        //Abrir calendario en el editText cuando se presiona sobre ellos
        txt_fechaDesde.setOnClickListener {
            abrirCalendario(txt_fechaDesde)
        }
        txt_fechaHasta.setOnClickListener {
            abrirCalendario(txt_fechaHasta)
        }
        btn_buscarDiadnostico.setOnClickListener {
            //Control de errores:fecha desde no puede ser superior a fecha hasta y no pueden quedar vacíos
            //Convertimos las fechas a Date para poder compararlas
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fechaDesdeString = txt_fechaDesde.text.toString()
            val fechaHastaString = txt_fechaHasta.text.toString()

            if (fechaDesdeString.isEmpty()) {
                Toast.makeText(this, "Debe seleccionar una fecha DESDE", Toast.LENGTH_SHORT).show()
            } else if (fechaHastaString.isEmpty()) {
                Toast.makeText(this, "Debe seleccionar una fecha HASTA", Toast.LENGTH_SHORT).show()
            } else {
                try {
                    val fechaDesde = formatoFecha.parse(fechaDesdeString)
                    var fechaHasta = formatoFecha.parse(fechaHastaString)

                    if (fechaDesde != null && fechaHasta != null && fechaDesde.after(fechaHasta)) {
                        Toast.makeText(
                            this,
                            "La fecha desde no puede ser superior a la fecha hasta",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(this, "Rango de fechas CORRECTO", Toast.LENGTH_SHORT).show()

                        //Limpiar el array por si hay resultados anteriores 8evitamos que se añadan
                        listaDiagnosticosSpinner.clear()
                        //Solicitamos los datos a la base de datos
                        cargarDatosEntreFechasSeleccionadas(
                            fechaEuropeaAMysql(fechaDesdeString),
                            fechaEuropeaAMysql(fechaHastaString)
                        )
                        montarSpinnerAdapter(listaDiagnosticosSpinner)
                        //Mostrar el spinner
                        spinner_diagnosticos.visibility = View.VISIBLE

                        //Gestión de los elementos seleccionados en el spinner
                        spinner_diagnosticos.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                val itemSeleccionado = parent.getItemAtPosition(position).toString()

                                if (itemSeleccionado != "Seleccione un diagnóstico" &&
                                    itemSeleccionado != "No existen diagnósticos disponibles"
                                ) {

                                    val elementos = itemSeleccionado.split(" - ")
                                     idSeleccionado = elementos[0].replace("#", "")
                                     fechaSeleccionada = elementos[1].replace("Fecha:", "")
                                     diagnosticoSeleccionado = elementos[2].replace("Diag.:", "")

                                    lbl_valorId.text = idSeleccionado
                                    lbl_valorFecha.text = fechaSeleccionada
                                    lbl_valorDiagnostico.text = diagnosticoSeleccionado
                                } else {
                                    lbl_valorId.text = ""
                                    lbl_valorFecha.text = ""
                                    lbl_valorDiagnostico.text = ""
                                }
                            }
                            override fun onNothingSelected(parent: AdapterView<*>) {
                                // Opcional: manejar si se deselecciona algo
                            }
                        }

                    }
                } catch (e: ParseException) {
                    Toast.makeText(this, "Error en el formato de fechas", Toast.LENGTH_SHORT).show()
                }
            }

        }
        btn_previsualizarInforme.setOnClickListener {
            val intent =Intent(this,GenerarInformeActivity::class.java)
            intent.putExtra("idDiagnostico", idSeleccionado)
            startActivity(intent)
        }

    }

    fun abrirCalendario(cuadroFecha: EditText) {
        val calendario = Calendar.getInstance()
        var anyo = calendario.get(Calendar.YEAR)
        var mes = calendario.get(Calendar.MONTH)
        var dia = calendario.get(Calendar.DAY_OF_MONTH)

        var dialogoFecha =
            DatePickerDialog(this@SeleccionarInformeActivity, R.style.Estilo_ColoresCalendario,
                object : DatePickerDialog.OnDateSetListener {
                    override fun onDateSet(datePicker: DatePicker, anyo: Int, mes: Int, dia: Int) {
                        var mesCorrecto = mes + 1
                        var fecha = "%02d/%02d/%04d".format(dia, mesCorrecto, anyo)
                        cuadroFecha.setText(fecha)
                    }
                }, anyo, mes, dia
            )
        dialogoFecha.show()
    }

    fun cargarDatosEntreFechasSeleccionadas(fechaDesde: String, fechaHasta: String) {
        //-----Cominucacion con la API-Rest-----------
        if (android.os.Build.VERSION.SDK_INT > 9) {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }
        var accesoRemotoDiagnosticos = AccesoRemotoDiagnosticos()
        result = accesoRemotoDiagnosticos.obtenerListadoEntreFechas(fechaDesde, fechaHasta)
        //verificamos que result no está vacio
        try {
            if (result.length() > 0) {
                for (i in 0 until result.length()) {
                    jsonObject = result.getJSONObject(i)
                    idDiagnosticoBD = jsonObject.getString("idDiagnostico")
                    fechaDiagnosticoBD = jsonObject.getString("fechaDiagnostico")
                    diagnosticoDiagnosticoBD = jsonObject.getString("diagnosticoDiagnostico")

                    listaDiagnosticosSpinner.add(
                        "#$idDiagnosticoBD - Fecha:${
                            fechaMysqlAEuropea(
                                fechaDiagnosticoBD
                            )
                        } - Diag.: $diagnosticoDiagnosticoBD"
                    )
                }
            } else {
                Log.e("MainActivity", "El JSONObject está vacío")
                listaDiagnosticosSpinner.add("No existen diagnósticos disponibles")
            }
        } catch (e: JSONException) {
            Log.e("MainActivity", "Error al procesar el JSON", e)
        }
    }

    //Metodo para pasar fechas Europeas a MySQL
    fun fechaEuropeaAMysql(fecha: String): String {
        lateinit var fechaTransformada: String
        var elementosFecha = fecha.split("/")
        if (elementosFecha.size == 3) {
            fechaTransformada = "${elementosFecha[2]}-${elementosFecha[1]}-${elementosFecha[0]}"
        } else {
            fechaTransformada = "Error al formatear fechas"
        }
        return fechaTransformada
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

    fun montarSpinnerAdapter(listaDiagnosticos: MutableList<String>) {
        if (listaDiagnosticos[0] != "No existen diagnósticos disponibles") {
            listaDiagnosticos.add(0, "Seleccione un diagnóstico")
        }
        adaptadorSpinner =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, listaDiagnosticos)
        adaptadorSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        //Asignar el adaptador al spinner
        spinner_diagnosticos.adapter = adaptadorSpinner
    }
}