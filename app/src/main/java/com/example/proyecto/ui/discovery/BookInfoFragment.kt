package com.example.proyecto.ui.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.databinding.FragmentBookInfoBinding

class BookInfoFragment : Fragment() {

    private var _binding: FragmentBookInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookInfoBinding.inflate(inflater, container, false)

        val args = arguments
        binding.tvBookTitle.text  = args?.getString("bookTitle")    ?: ""
        binding.tvAuthor.text     = args?.getString("bookAuthor")   ?: ""
        binding.tvYear.text       = args?.getString("bookYear")     ?: ""
        binding.tvOwnerName.text  = args?.getString("ownerName")    ?: ""

        val pages    = args?.getString("bookPages")    ?: ""
        val language = args?.getString("bookLanguage") ?: ""
        binding.tvPages.text    = "$pages Pages"
        binding.tvLanguage.text = language

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnRequestLoan.setOnClickListener {
            // TODO: implementar lógica de solicitud de préstamo
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
