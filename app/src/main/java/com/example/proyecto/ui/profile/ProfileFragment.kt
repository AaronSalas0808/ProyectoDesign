package com.example.proyecto.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentProfileUserBinding
import com.example.proyecto.network.BookRepository
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileUserBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileUserBinding.inflate(inflater, container, false)

        val ownerName = arguments?.getString("ownerName") ?: "Elena Rodriguez"
        binding.tvProfileName.text = ownerName

        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.btnMessage.setOnClickListener {
            val bundle = Bundle().apply { putString("ownerName", ownerName) }
            findNavController().navigate(R.id.action_profile_owner_to_chat, bundle)
        }

        binding.rvOwnerBooks.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        loadOwnerBooks(ownerName)

        return binding.root
    }

    private fun loadOwnerBooks(ownerName: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val books = BookRepository.getBooks()
                    .filter { it.ownerName.equals(ownerName, ignoreCase = true) }

                binding.rvOwnerBooks.adapter = OwnerBookAdapter(books) { book ->
                    val bundle = Bundle().apply {
                        putString("bookTitle",    book.title)
                        putString("bookAuthor",   book.author)
                        putString("bookYear",     book.year)
                        putString("bookPages",    book.pages)
                        putString("bookLanguage", book.language)
                        putString("ownerName",    book.ownerName)
                    }
                    findNavController().navigate(R.id.action_profile_owner_to_book_info, bundle)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error cargando libros", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
