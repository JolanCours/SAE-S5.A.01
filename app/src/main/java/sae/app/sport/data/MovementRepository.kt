package sae.app.sport.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import sae.app.sport.model.MovementSequence

class MovementRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("movement_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY_MOVEMENTS = "key_movements"

    fun getAllMovements(): List<MovementSequence> {
        val json = prefs.getString(KEY_MOVEMENTS, null) ?: return emptyList()
        val type = object : TypeToken<List<MovementSequence>>() {}.type
        return try {
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveMovement(movement: MovementSequence) {
        val list = getAllMovements().toMutableList()
        list.removeAll { it.id == movement.id }
        list.add(movement)
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_MOVEMENTS, json).apply()
    }

    fun deleteMovement(id: String) {
        val list = getAllMovements().toMutableList()
        list.removeAll { it.id == id }
        val json = gson.toJson(list)
        prefs.edit().putString(KEY_MOVEMENTS, json).apply()
    }
}
