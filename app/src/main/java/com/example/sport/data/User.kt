package com.example.sport.data

/**
 * Modèle représentant un utilisateur dans la base de données locale.
 */
data class User(
    val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val username: String,
    val email: String,
    val passwordHash: String
)
