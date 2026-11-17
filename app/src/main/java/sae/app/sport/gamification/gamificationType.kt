package sae.app.sport.gamification

// Les deux types de gamification proposés
enum class GamificationType(val label: String, val description: String) {
    BADGES("Badges", "Des badges à débloquer selon tes exercices"),
    POINTS("Points et niveaux", "Des points et un niveau qui monte")
}
