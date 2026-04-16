package com.example.proyecto.ui.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
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

        val ownerClickListener = View.OnClickListener {
            val bundle = Bundle().apply {
                putString("ownerName", args?.getString("ownerName") ?: "")
            }
            findNavController().navigate(R.id.action_book_info_to_profile_owner, bundle)
        }
        binding.ivOwnerProfile.setOnClickListener(ownerClickListener)
        binding.tvOwnerName.setOnClickListener(ownerClickListener)

        binding.btnRequestLoan.setOnClickListener {
            val bookTitle = args?.getString("bookTitle") ?: ""
            val ownerName = args?.getString("ownerName") ?: ""
            val bundle = Bundle().apply {
                putString("ownerName", ownerName)
                putString("defaultMessage", "Hola estoy interesado en poder leer $bookTitle ¿se encuentra disponible?")
            }
            findNavController().navigate(R.id.action_book_info_to_chat, bundle)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
