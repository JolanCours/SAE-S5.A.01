package com.example.myapplication;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Base de données principale Room de l'application.
 * Version augmentée à 4 pour forcer la remise à zéro des données et afficher les nouvelles images.
 */
@Database(entities = {Badge.class}, version = 4, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    public abstract BadgeDao badgeDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "sport_gamification_db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
