package com.example.myapplication;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptateur pour le RecyclerView (Couche View de l'architecture MVVM).
 * Il convertit les objets de données {@link Badge} en éléments visuels affichables dans la liste.
 */
public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    // Liste locale stockant les badges à afficher
    private List<Badge> badgeList = new ArrayList<>();

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        Badge currentBadge = badgeList.get(position);
        holder.titleTextView.setText(currentBadge.title);
        holder.descTextView.setText(currentBadge.description);
        
        // --- GESTION DE L'IMAGE ---
        if (currentBadge.imageResId != 0) {
            holder.badgeImageView.setImageResource(currentBadge.imageResId);
        } else {
            // Image par défaut si aucune n'est fournie (évite les bugs de recyclage)
            holder.badgeImageView.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // --- GESTION DU PROGRÈS ---
        holder.progressBar.setMax(currentBadge.totalGoal);
        holder.progressBar.setProgress(currentBadge.currentProgress);
        String progressText = currentBadge.currentProgress + " / " + currentBadge.totalGoal;
        holder.ratioTextView.setText(progressText);

        // --- GESTION DU STATUT DÉBLOQUÉ/VERROUILLÉ ---
        if (currentBadge.isUnlocked) {
            holder.titleTextView.setTextColor(Color.parseColor("#4CAF50"));
            holder.descTextView.setText(currentBadge.description + " - [Débloqué]");
            holder.progressBar.setProgress(currentBadge.totalGoal);
            
            // Image en couleur et opaque
            holder.badgeImageView.clearColorFilter();
            holder.badgeImageView.setAlpha(1.0f);
        } else {
            holder.titleTextView.setTextColor(Color.GRAY);
            holder.descTextView.setText(currentBadge.description + " - [Verrouillé]");
            
            // Appliquer un filtre noir et blanc
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
            holder.badgeImageView.setColorFilter(filter);
            
            // Image semi-transparente
            holder.badgeImageView.setAlpha(0.5f);
        }
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public void setBadges(List<Badge> badges) {
        this.badgeList = badges;
        notifyDataSetChanged();
    }

    static class BadgeViewHolder extends RecyclerView.ViewHolder {
        ImageView badgeImageView;
        TextView titleTextView;
        TextView descTextView;
        ProgressBar progressBar;
        TextView ratioTextView;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            badgeImageView = itemView.findViewById(R.id.imageBadgeIcon);
            titleTextView = itemView.findViewById(R.id.textBadgeTitle);
            descTextView = itemView.findViewById(R.id.textBadgeDescription);
            progressBar = itemView.findViewById(R.id.progressBadge);
            ratioTextView = itemView.findViewById(R.id.textBadgeProgressRatio);
        }
    }
}
