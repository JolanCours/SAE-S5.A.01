package sae.app.sport.data

import android.content.Context

// Sauvegarde les paramètres dans le téléphone (SharedPreferences)
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Lit / écrit le paramètre "éléments de jeu" (activé par défaut)
    var gamificationEnabled: Boolean
        get() = prefs.getBoolean("gamification_enabled", true)
        set(value) = prefs.edit().putBoolean("gamification_enabled", value).apply()
}