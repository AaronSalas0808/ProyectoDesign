package com.example.proyecto.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentProfileBinding
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
        binding.ivBurgerMenu.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        binding.btnAddBook.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_add)
        }

        binding.tvViewAll.setOnClickListener {
            // TODO: ver todos los libros disponibles
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            (requireActivity() as MainActivity).logoutUser()
            return
        }

        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name").orEmpty()
                val username = document.getString("username").orEmpty()

                val displayName = when {
                    name.isNotBlank() -> name
                    !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                    !currentUser.email.isNullOrBlank() -> currentUser.email!!
                    else -> "Usuario"
                }

                val displayHandle = when {
                    username.isNotBlank() -> "@$username"
                    !currentUser.email.isNullOrBlank() -> "@${currentUser.email!!.substringBefore("@")}"
                    else -> "@usuario"
                }

                binding.tvProfileName.text = displayName
                binding.tvUsername.text = displayHandle
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "No se pudo cargar el perfil: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()

                val fallbackName = when {
                    !currentUser.displayName.isNullOrBlank() -> currentUser.displayName!!
                    !currentUser.email.isNullOrBlank() -> currentUser.email!!
                    else -> "Usuario"
                }

                val fallbackHandle = when {
                    !currentUser.email.isNullOrBlank() -> "@${currentUser.email!!.substringBefore("@")}"
                    else -> "@usuario"
                }

                binding.tvProfileName.text = fallbackName
                binding.tvUsername.text = fallbackHandle
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}