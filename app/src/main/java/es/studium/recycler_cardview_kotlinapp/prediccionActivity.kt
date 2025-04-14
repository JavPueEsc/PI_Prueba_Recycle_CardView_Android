package es.studium.recycler_cardview_kotlinapp

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import es.studium.recycler_cardview_kotlinapp.databinding.ActivityPrediccionBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Locale

typealias LumaListener = (luma: Double) -> Unit

class prediccionActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityPrediccionBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    // realizar predicción (3)
    private lateinit var modelPredictor: ModelPredictor

    //cargar imagen (1)
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewBinding.photoView.setImageURI(it)
        }
    }
    private lateinit var prediccion : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityPrediccionBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Inicializa el ModelPredictor(3)
        modelPredictor = ModelPredictor(this)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listeners for take photo and video capture buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        //cargar una imagen (2)
        viewBinding.galleryButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // Configura el listener para la predicción
        viewBinding.predictionButton.setOnClickListener {
            // Obtener la URI de la imagen cargada en el ImageView
            val imageUri = getImageUriFromImageView()

            if (imageUri != null) {
                try {
                    // Realizar la predicción utilizando ModelPredictor
                    val (predictedClass, confidence) = modelPredictor.predict(imageUri)
                    val message = "Predicción: $predictedClass (Confianza: %.2f%%)".format(confidence)
                    prediccion = predictedClass
                    // Mostrar el resultado en un Toast
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    // En caso de error al procesar la imagen
                    Toast.makeText(this, "Error al cargar o procesar la imagen", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Error al procesar la imagen", e)
                }
            } else {
                Toast.makeText(this, "No se ha cargado ninguna imagen", Toast.LENGTH_SHORT).show()
            }

            //pasamos al activity de alta
            var fechaActual = LocalDate.now().toString()
            var uriImagen = imageUri.toString()

            var bundle = Bundle()
            bundle.putString("imagenDiagnostico",uriImagen)
            bundle.putString("fechaDiagnostico",fechaActual)
            bundle.putString("diagnosticoDiagnostico",prediccion)

            var intent = Intent(this,AltaActivity::class.java)
            intent.putExtras(bundle)
            startActivity(intent)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    //Predicción (3)
    private fun getImageUriFromImageView(): Uri? {
        // Obtener la URI de la imagen mostrada en el ImageView
        return (viewBinding.photoView.drawable as? BitmapDrawable)?.let {
            val bitmap = it.bitmap
            val tempUri = getImageUri(bitmap)
            tempUri
        }
    }
    //Predicción (3)
    private fun getImageUri(bitmap: Bitmap): Uri? {
        // Convertir el bitmap a URI (guardar en almacenamiento temporal)
        val path = MediaStore.Images.Media.insertImage(contentResolver, bitmap, "temp_image", null)
        return Uri.parse(path)
    }

    // Función para redimensionar la imagen (4)
    private fun resizeImage(image: Bitmap): Bitmap {
        return Bitmap.createScaledBitmap(image, 224, 224, true)
    }


    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)

                    // Mostrar la imagen en el ImageView
                    savedUri?.let {
                        viewBinding.photoView.setImageURI(it)
                    }
                }
            }
        )
    }

    private fun captureVideo() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}