package com.example.myapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Modèle de données représentant un Badge (Couche Model de l'architecture MVVM).
 * Cette classe est annotée avec @Entity pour indiquer à Room qu'elle doit créer une table SQL.
 */
@Entity(tableName = "badges")
public class Badge {
    
    // Clé primaire auto-générée de manière unique pour chaque badge inséré
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Titre du badge (ex: "Premier Pas")
    public String title;
    
    // Description des conditions d'obtention du badge
    public String description;
    
    // État du badge : true si l'utilisateur l'a débloqué, false sinon
    public boolean isUnlocked;

    // Progrès actuel de l'utilisateur pour ce badge
    public int currentProgress;

    // Objectif total à atteindre pour débloquer le badge
    public int totalGoal;

    // ID de la ressource image associée au badge (ex: R.drawable.ic_badge_1)
    public int imageResId;

    /**
     * Constructeur utilisé par Room et l'application pour instancier un nouveau Badge.
     */
    public Badge(String title, String description, boolean isUnlocked, int currentProgress, int totalGoal, int imageResId) {
        this.title = title;
        this.description = description;
        this.isUnlocked = isUnlocked;
        this.currentProgress = currentProgress;
        this.totalGoal = totalGoal;
        this.imageResId = imageResId;
    }
}
