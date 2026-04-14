package com.example.proyecto.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
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
        binding.tvProfileName.text = ownerName

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnMessage.setOnClickListener {
            val bundle = Bundle().apply {
                putString("ownerName", ownerName)
            }
            findNavController().navigate(R.id.action_profile_owner_to_chat, bundle)
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
