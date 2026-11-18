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
import com.example.sport.data.UserDatabaseHelper
import sae.app.sport.MainActivity
import sae.app.sport.R

class LoginFragment : Fragment(R.layout.fragment_login) {
    private lateinit var dbHelper: UserDatabaseHelper

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbHelper = UserDatabaseHelper(requireContext())

        val etIdentifier = view.findViewById<EditText>(R.id.etIdentifier)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)

        btnLogin.setOnClickListener {
            val identifier = etIdentifier.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (identifier.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Veuillez saisir votre identifiant et mot de passe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = dbHelper.authenticateUser(identifier, password)
            if (user != null) {
                Toast.makeText(requireContext(), "Bienvenue ${user.firstName} !", Toast.LENGTH_SHORT).show()
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Identifiant ou mot de passe incorrect", Toast.LENGTH_SHORT).show()
            }
        }

        tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
}
