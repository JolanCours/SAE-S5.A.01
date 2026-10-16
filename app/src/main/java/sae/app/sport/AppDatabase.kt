package sae.app.sport

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de données principale Room en Kotlin.
 * Gère la persistance locale des données des badges pour l'application sport.
 */
@Database(entities = [Badge::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * Fournit l'accès aux opérations DAO pour les badges.
     */
    abstract fun badgeDao(): BadgeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Récupère l'instance unique (Singleton) de la base de données Room.
         *
         * @param context Le contexte de l'application.
         * @return L'instance de [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sport_gamification_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
