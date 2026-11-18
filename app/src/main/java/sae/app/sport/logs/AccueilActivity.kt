package sae.app.sport

import android.content.Intent
import android.os.Bundle
import android.util.Log // 1. Importer Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import sae.app.sport.logs.recap_sceance

class AccueilActivity : AppCompatActivity() {

    // Définir un TAG pour retrouver facilement tes logs dans Logcat
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_accueil)

        Log.d(TAG, "onCreate: AccueilActivity lancée avec succès") // Log de debug

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnVoirBilan = findViewById<Button>(R.id.btnVoirBilan)

        btnVoirBilan.setOnClickListener {
            Log.i(TAG, "Clic sur le bouton 'Voir le bilan'") // Log d'information
            val intent = Intent(this, recap_sceance::class.java)
            startActivity(intent)
        }
    }
}
