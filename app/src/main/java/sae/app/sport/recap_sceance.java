package sae.app.sport;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // 1. Importer Log
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class recap_sceance extends AppCompatActivity {

    // Définir un TAG
    private static final String TAG = "RecapSeance";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_recap_sceance);

        Log.d(TAG, "onCreate: recap_sceance lancée");

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Afficher la date et l'heure actuelles dans textView2
        android.widget.TextView tvDate = findViewById(R.id.textView2);
        String currentDate = new java.text.SimpleDateFormat("dd/MM/yyyy       HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        tvDate.setText(getString(R.string.recap_date_format, currentDate));

        Button btn = findViewById(R.id.button);
        btn.setOnClickListener(v -> {
            Log.i(TAG, "Clic sur le bouton 'Retour à l'accueil'");
            Intent intent = new Intent(recap_sceance.this, AccueilActivity.class);
            startActivity(intent);
        });

        Button new_seance = findViewById(R.id.button2);
        new_seance.setOnClickListener(v -> {
            Log.i(TAG, "Clic sur le bouton 'Refaire la séance'");
            Intent intent = new Intent(recap_sceance.this, AccueilActivity.class);
            startActivity(intent);
        });

        Button btnExportLogs = findViewById(R.id.button3);
        btnExportLogs.setOnClickListener(v -> {
            Log.i(TAG, "Clic sur 'Importer les logs'");
            exportLogsToFile();
        });
    }

    private void exportLogsToFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TITLE, "seance_logs.txt");
        startActivityForResult(intent, 100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            android.net.Uri uri = data.getData();
            if (uri != null) {
                try (java.io.OutputStream os = getContentResolver().openOutputStream(uri)) {
                    if (os != null) {
                        try (java.io.PrintWriter writer = new java.io.PrintWriter(os)) {
                            for (String log : SessionLogManager.getLogs()) {
                                writer.println(log);
                            }
                            android.widget.Toast.makeText(this, "Logs exportés avec succès !", android.widget.Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "Logs exportés avec succès vers le fichier sélectionné");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur export logs", e);
                    android.widget.Toast.makeText(this, "Erreur lors de l'export des logs", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}