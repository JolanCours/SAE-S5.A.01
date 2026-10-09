package sae.app.sport.Objectif

data class Objectif(
    val nom: String,
    val exercices: MutableList<Exercice> = mutableListOf()

)