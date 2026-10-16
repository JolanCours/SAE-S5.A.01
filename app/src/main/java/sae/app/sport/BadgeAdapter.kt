package sae.app.sport

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptateur pour le RecyclerView (Kotlin).
 * Permet d'afficher la liste des badges dans une interface de type liste (Vues XML).
 * Gère l'affichage dynamique (état verrouillé/débloqué, progression, images en N&B si verrouillé).
 */
class BadgeAdapter : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    private var badgeList: List<Badge> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_badge, parent, false)
        return BadgeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val currentBadge = badgeList[position]
        holder.titleTextView.text = currentBadge.title
        holder.descTextView.text = currentBadge.description

        // Gestion de l'image du badge
        if (currentBadge.imageResId != 0) {
            holder.badgeImageView.setImageResource(currentBadge.imageResId)
        } else {
            holder.badgeImageView.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // Gestion de la barre de progression
        holder.progressBar.max = currentBadge.totalGoal
        holder.progressBar.progress = currentBadge.currentProgress
        holder.ratioTextView.text = "${currentBadge.currentProgress} / ${currentBadge.totalGoal}"

        // Personnalisation visuelle selon l'état (débloqué ou verrouillé)
        if (currentBadge.isUnlocked) {
            holder.titleTextView.setTextColor(Color.parseColor("#4CAF50"))
            holder.descTextView.text = "${currentBadge.description} - [Débloqué]"
            holder.progressBar.progress = currentBadge.totalGoal
            
            holder.badgeImageView.clearColorFilter()
            holder.badgeImageView.alpha = 1.0f
        } else {
            holder.titleTextView.setTextColor(Color.GRAY)
            holder.descTextView.text = "${currentBadge.description} - [Verrouillé]"
            
            // Applique un filtre Noir & Blanc si le badge est verrouillé
            val matrix = ColorMatrix().apply { setSaturation(0f) }
            holder.badgeImageView.colorFilter = ColorMatrixColorFilter(matrix)
            holder.badgeImageView.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int = badgeList.size

    /**
     * Met à jour la liste des badges affichés par l'adaptateur.
     */
    fun setBadges(badges: List<Badge>) {
        this.badgeList = badges
        notifyDataSetChanged()
    }

    /**
     * ViewHolder représentant les vues d'un élément de badge individuel.
     */
    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val badgeImageView: ImageView = itemView.findViewById(R.id.imageBadgeIcon)
        val titleTextView: TextView = itemView.findViewById(R.id.textBadgeTitle)
        val descTextView: TextView = itemView.findViewById(R.id.textBadgeDescription)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBadge)
        val ratioTextView: TextView = itemView.findViewById(R.id.textBadgeProgressRatio)
    }
}
