package es.studium.recycler_cardview_kotlinapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.TextView

class AdaptadorDiagnosticos(
    var listaDiagnosticos: MutableList<ModeloDiagnostico>
) : RecyclerView.Adapter<AdaptadorDiagnosticos.MyViewHolder>() {



    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lbl_valorIdDiag: TextView = itemView.findViewById(R.id.valor_id)
        val img_imagenDiag: ImageView = itemView.findViewById(R.id.imagen)
        val lbl_valorFechaDiag: TextView = itemView.findViewById(R.id.valor_fechaDiag)
        val lbl_valorDiag: TextView = itemView.findViewById(R.id.valor_diagnostico)
        val lbl_gravedadDiag: TextView = itemView.findViewById(R.id.valor_valoracion)
        val lbl_doctorDiag: TextView = itemView.findViewById(R.id.valor_doctor)
        val lbl_centroDiag: TextView = itemView.findViewById(R.id.valor_centro)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AdaptadorDiagnosticos.MyViewHolder {
        var filaDiagnostico: View =
            LayoutInflater.from(parent.context).inflate(R.layout.tarjeta_diagnostico, parent, false)
        return MyViewHolder(filaDiagnostico)
    }

    override fun onBindViewHolder(holder: AdaptadorDiagnosticos.MyViewHolder, position: Int) {
        //obtener el diagnostico de la lista gracias al indice position
        var diagnostico: ModeloDiagnostico = listaDiagnosticos[position]
        //Obtener los datos del diagnostico
        var idDiagnostico: String = diagnostico.idDiagnostico
        var imagenDiagnostico: ByteArray = diagnostico.imagenDiagnostico
        var fechaDiagnostico : String = diagnostico.fechaDiagnostico
        var diagnosticoDiagnostico: String = diagnostico.diagnosticoDiagnostico
        var gravedadDiagnostico: String = diagnostico.gravedadDiagnostico
        var doctorDiagnostico: String = diagnostico.doctorDiagnostico
        var centroDiagnostico: String = diagnostico.centroDiagnostico

        //Setear los datos en sus respectivos campos
        holder.lbl_valorIdDiag.text = idDiagnostico
        //Pasar imagen de ByteArray a Bitmap para poder mostrarla
        val imagenBitmap = byteArrayABitmap(imagenDiagnostico)
        if (imagenBitmap != null) {
            holder.img_imagenDiag.setImageBitmap(imagenBitmap)
        } else {
            holder.img_imagenDiag.setImageResource(R.drawable.imagen_por_defecto) // opcional
        }
        holder.lbl_valorFechaDiag.text = fechaDiagnostico
        holder.lbl_valorDiag.text = diagnosticoDiagnostico
        holder.lbl_gravedadDiag.text = gravedadDiagnostico
        holder.lbl_doctorDiag.text = doctorDiagnostico
        holder.lbl_centroDiag.text = centroDiagnostico

        Log.d("IMAGEN_RECIBIDA", "Tamaño byte array: ${imagenDiagnostico.size}")

    }

    override fun getItemCount(): Int {
        return listaDiagnosticos.size
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
