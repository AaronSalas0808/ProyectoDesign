package com.example.proyecto.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentProfileBinding
import com.example.proyecto.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileUserFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClicks()
        loadUserProfile()

        return binding.root
    }

    private fun setupClicks() {
        binding.btnAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_add)
        }

        binding.tvViewAll.setOnClickListener {
            // TODO: ver todos los libros disponibles
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            goToLogin()
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            goToLogin()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name").orEmpty()
                    val username = document.getString("username").orEmpty()

                    binding.tvProfileName.text =
                        if (name.isNotBlank()) name else "Usuario"

                    binding.tvUsername.text =
                        if (username.isNotBlank()) "@$username"
                        else currentUser.email ?: "@usuario"
                } else {
                    binding.tvProfileName.text = currentUser.email ?: "Usuario"
                    binding.tvUsername.text = currentUser.email ?: "@usuario"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar el perfil: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                binding.tvProfileName.text = currentUser.email ?: "Usuario"
                binding.tvUsername.text = currentUser.email ?: "@usuario"
            }
    }

    private fun goToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}