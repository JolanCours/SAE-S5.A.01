package com.example.sport

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.sport.data.User
import com.example.sport.data.UserDatabaseHelper
import sae.app.sport.MainActivity
import sae.app.sport.R

class RegisterFragment : Fragment(R.layout.fragment_register) {
    private lateinit var dbHelper: UserDatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = UserDatabaseHelper(requireContext())

        val etFirstName = view.findViewById<EditText>(R.id.etFirstName)
        val etLastName = view.findViewById<EditText>(R.id.etLastName)
        val etUsername = view.findViewById<EditText>(R.id.etUsername)
        val etEmail = view.findViewById<EditText>(R.id.etRegEmail)
        val etPassword = view.findViewById<EditText>(R.id.etRegPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvGoToLogin = view.findViewById<TextView>(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (dbHelper.isEmailOrUsernameTaken(email, username)) {
                Toast.makeText(requireContext(), "Cet email ou nom d'utilisateur est déjà utilisé", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(
                firstName = firstName,
                lastName = lastName,
                username = username,
                email = email,
                passwordHash = password
            )

            val result = dbHelper.registerUser(newUser)
            if (result != -1L) {
                Toast.makeText(requireContext(), "Compte créé avec succès !", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Erreur lors de la création du compte", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
}
