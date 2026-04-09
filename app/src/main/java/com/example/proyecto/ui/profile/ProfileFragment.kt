package com.example.proyecto.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.databinding.FragmentProfileUserBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileUserBinding.inflate(inflater, container, false)

        val args = arguments
        val ownerName   = args?.getString("ownerName") ?: "Elena Rodriguez"
        val ownerRating = args?.getFloat("ownerRating") ?: 4.8f

        binding.tvProfileName.text = ownerName
        binding.tvRating.text      = ownerRating.toString()

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMessage.setOnClickListener {
            // TODO: abrir chat con el dueño
        }

        binding.tvViewAll.setOnClickListener {
            // TODO: ver todos los libros del dueño
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
