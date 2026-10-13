package sae.app.sport

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Récupère le bouton défini dans activity_main.xml
        val btnVoirBilan = findViewById<Button>(R.id.btnVoirBilan)

        // 3. Ouvre l'activité de bilan au clic
        btnVoirBilan.setOnClickListener {
            // Remplace "recap_sceance" par le nom exact de ta classe Java/Kotlin de bilan
            val intent = Intent(this, recap_sceance::class.java)
            startActivity(intent)
        }
    }
}