package com.example.sport.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Helper SQLite pour gérer la création, l'inscription et l'authentification des utilisateurs en local.
 */
class UserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "user_database.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_USERS = "users"
        const val COLUMN_ID = "id"
        const val COLUMN_FIRST_NAME = "first_name"
        const val COLUMN_LAST_NAME = "last_name"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = ("CREATE TABLE " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_FIRST_NAME + " TEXT, "
                + COLUMN_LAST_NAME + " TEXT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_EMAIL + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT)")
        db.execSQL(createTable)

        // Compte par défaut pour tester la connexion immédiatement
        val values = ContentValues().apply {
            put(COLUMN_FIRST_NAME, "Test")
            put(COLUMN_LAST_NAME, "User")
            put(COLUMN_USERNAME, "testuser")
            put(COLUMN_EMAIL, "test@example.com")
            put(COLUMN_PASSWORD, "password123")
        }
        db.insert(TABLE_USERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     * @return L'ID du nouvel utilisateur ou -1 en cas d'échec.
     */
    fun registerUser(user: User): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_FIRST_NAME, user.firstName)
            put(COLUMN_LAST_NAME, user.lastName)
            put(COLUMN_USERNAME, user.username)
            put(COLUMN_EMAIL, user.email)
            put(COLUMN_PASSWORD, user.passwordHash)
        }
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    /**
     * Vérifie si un email ou un nom d'utilisateur est déjà enregistré.
     */
    fun isEmailOrUsernameTaken(email: String, username: String): Boolean {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE $COLUMN_EMAIL = ? OR $COLUMN_USERNAME = ?"
        val cursor = db.rawQuery(query, arrayOf(email.trim(), username.trim()))
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    /**
     * Authentifie un utilisateur via son email ou son nom d'utilisateur et son mot de passe.
     * @return L'instance de [User] si valide, ou null sinon.
     */
    fun authenticateUser(identifier: String, password: String): User? {
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_USERS WHERE ($COLUMN_EMAIL = ? OR $COLUMN_USERNAME = ?) AND $COLUMN_PASSWORD = ?"
        val cursor = db.rawQuery(query, arrayOf(identifier.trim(), identifier.trim(), password))

        var user: User? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID))
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME))
            val username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            val pwd = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD))

            user = User(id, firstName, lastName, username, email, pwd)
        }
        cursor.close()
        db.close()
        return user
    }
}
