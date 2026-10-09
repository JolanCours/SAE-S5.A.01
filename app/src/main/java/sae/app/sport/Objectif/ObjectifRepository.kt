package sae.app.sport.Objectif

object ObjectifRepository {

    val objectifs = mutableListOf(
        Objectif(
            nom = "Perdre du poids",
            exercices = mutableListOf(
                Exercice(
                    nom = "Course",
                    type = TypeExercice.TEMPS,
                    valeur = 30
                ),
                Exercice(
                    nom = "Pompes",
                    type = TypeExercice.REPETITIONS,
                    valeur = 15
                )
            )
        ),

        Objectif(
            nom = "Améliorer mon endurance",
            exercices = mutableListOf(
                Exercice(
                    nom = "Course",
                    type = TypeExercice.TEMPS,
                    valeur = 45
                )
            )
        )
    )
}