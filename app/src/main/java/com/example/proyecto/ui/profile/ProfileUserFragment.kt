package com.example.proyecto.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentProfileBinding
import com.example.proyecto.session.SessionManager
import java.io.File

class ProfileUserFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        sessionManager = SessionManager(requireContext())

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
            // Aquí puedes navegar a una lista completa de libros del usuario si luego la agregas.
        }
    }

    private fun loadUserProfile() {
        if (!sessionManager.isLoggedIn()) {
            (requireActivity() as MainActivity).logoutUser()
            return
        }

        binding.tvProfileName.text = sessionManager.getDisplayName()
        binding.tvUsername.text = sessionManager.getDisplayEmail()

        val photoPath = sessionManager.getProfilePhotoPath()
        if (photoPath.isNotBlank()) {
            val file = File(photoPath)
            if (file.exists()) {
                binding.ivProfilePhoto.setImageURI(Uri.fromFile(file))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (::sessionManager.isInitialized) {
            loadUserProfile()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}