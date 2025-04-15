package es.studium.recycler_cardview_kotlinapp

data class ModeloDiagnostico (
    var idDiagnostico : String,
    //Para la imagen, ya que es una variable binaria
    var imagenDiagnostico : ByteArray ,
    var fechaDiagnostico : String ,
    var diagnosticoDiagnostico : String ,
    var gravedadDiagnostico : String ,
    var doctorDiagnostico : String ,
    var centroDiagnostico : String )
