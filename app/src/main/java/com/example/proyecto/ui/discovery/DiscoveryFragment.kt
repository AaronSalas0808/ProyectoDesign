package com.example.proyecto.ui.discovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentDiscoveryBinding

class DiscoveryFragment : Fragment() {

    private var _binding: FragmentDiscoveryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this).get(DiscoveryViewModel::class.java)

        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)

        binding.ivMenuBurger.setOnClickListener {
            (requireActivity() as com.example.proyecto.MainActivity).openDrawer()
        }

        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                android.widget.Toast.makeText(requireContext(), "Error: $error", android.widget.Toast.LENGTH_LONG).show()
            }
        }

        viewModel.books.observe(viewLifecycleOwner) { books ->
            binding.rvBooks.adapter = BookAdapter(
                books = books,
                onBookClick = { book ->
                    val bundle = Bundle().apply {
                        putString("bookTitle",    book.title)
                        putString("bookAuthor",   book.author)
                        putString("bookYear",     book.year)
                        putString("bookPages",    book.pages)
                        putString("bookLanguage", book.language)
                        putString("ownerName",    book.ownerName)
                    }
                    findNavController().navigate(R.id.action_discovery_to_book_info, bundle)
                },
                onOwnerClick = { book ->
                    val bundle = Bundle().apply {
                        putString("ownerName",   book.ownerName)
                    }
                    findNavController().navigate(R.id.action_discovery_to_profile_owner, bundle)
                }
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
