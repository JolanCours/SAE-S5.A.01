package sae.app.sport.data

import android.content.Context
import sae.app.sport.gamification.GamificationType

class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // Switch général (activé par défaut)
    var gamificationEnabled: Boolean
        get() = prefs.getBoolean("gamification_enabled", true)
        set(value) = prefs.edit().putBoolean("gamification_enabled", value).apply()

    var gamificationType: GamificationType
        get() {
            val saved = prefs.getString("gamification_type", GamificationType.BADGES.name)
            // Si la valeur est inconnue, on retombe sur BADGES
            return GamificationType.values().firstOrNull { it.name == saved }
                ?: GamificationType.BADGES
        }
        set(value) = prefs.edit().putString("gamification_type", value.name).apply()
}
