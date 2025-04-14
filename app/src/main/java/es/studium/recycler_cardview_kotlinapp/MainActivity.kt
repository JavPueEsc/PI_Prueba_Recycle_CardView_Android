package es.studium.recycler_cardview_kotlinapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var btnAlta : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAlta=findViewById(R.id.btnAlta)
        btnAlta.setOnClickListener {
            var intent = Intent(this,prediccionActivity::class.java)
            startActivity(intent)
        }
    }
}