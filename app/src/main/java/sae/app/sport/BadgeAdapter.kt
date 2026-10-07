package com.example.myapplication

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

        // Gestion de l'image
        if (currentBadge.imageResId != 0) {
            holder.badgeImageView.setImageResource(currentBadge.imageResId)
        } else {
            holder.badgeImageView.setImageResource(R.drawable.ic_launcher_foreground)
        }

        // Gestion du progrès
        holder.progressBar.max = currentBadge.totalGoal
        holder.progressBar.progress = currentBadge.currentProgress
        holder.ratioTextView.text = "${currentBadge.currentProgress} / ${currentBadge.totalGoal}"

        if (currentBadge.isUnlocked) {
            holder.titleTextView.setTextColor(Color.parseColor("#4CAF50"))
            holder.descTextView.text = "${currentBadge.description} - [Débloqué]"
            holder.progressBar.progress = currentBadge.totalGoal
            
            holder.badgeImageView.clearColorFilter()
            holder.badgeImageView.alpha = 1.0f
        } else {
            holder.titleTextView.setTextColor(Color.GRAY)
            holder.descTextView.text = "${currentBadge.description} - [Verrouillé]"
            
            val matrix = ColorMatrix().apply { setSaturation(0f) }
            holder.badgeImageView.colorFilter = ColorMatrixColorFilter(matrix)
            holder.badgeImageView.alpha = 0.5f
        }
    }

    override fun getItemCount(): Int = badgeList.size

    fun setBadges(badges: List<Badge>) {
        this.badgeList = badges
        notifyDataSetChanged()
    }

    class BadgeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val badgeImageView: ImageView = itemView.findViewById(R.id.imageBadgeIcon)
        val titleTextView: TextView = itemView.findViewById(R.id.textBadgeTitle)
        val descTextView: TextView = itemView.findViewById(R.id.textBadgeDescription)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBadge)
        val ratioTextView: TextView = itemView.findViewById(R.id.textBadgeProgressRatio)
    }
}
