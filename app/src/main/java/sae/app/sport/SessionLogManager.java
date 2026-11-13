package sae.app.sport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SessionLogManager {
    private static final List<String> logs = new ArrayList<>();

    public static void addLog(String message) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        logs.add("[" + timestamp + "] " + message);
    }

    public static List<String> getLogs() {
        if (logs.isEmpty()) {
            // Ajouter des logs par défaut si aucun n'a été enregistré
            addLog("Séance démarrée.");
            addLog("Exercice de chronomètre et répétitions initialisé.");
            addLog("Séance terminée et récapitulatif généré.");
        }
        return new ArrayList<>(logs);
    }

    public static void clearLogs() {
        logs.clear();
    }
}
