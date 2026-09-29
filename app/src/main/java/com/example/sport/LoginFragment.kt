package com.example.sport

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.fragment_login) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)
        btnLogin.setOnClickListener { Toast.makeText(requireContext(), "Connexion cliquée !", Toast.LENGTH_SHORT).show() }
        tvGoToRegister.setOnClickListener { findNavController().navigate(R.id.action_loginFragment_to_registerFragment) }
    }
}