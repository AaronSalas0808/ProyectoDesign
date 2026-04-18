package com.example.proyecto.ui.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentAddBinding
import com.example.proyecto.network.LocalDataStore
import com.example.proyecto.ui.discovery.Book

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private var selectedCondition: String = "Excellent"
    private var selectedCoverUri: android.net.Uri? = null

    private val pickCoverPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedCoverUri = uri
            binding.ivBookCoverPreview.setImageURI(uri)
            binding.ivBookCoverPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.coverUploadContainer.setOnClickListener {
            pickCoverPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        setupConditionChips()

        val genres = resources.getStringArray(R.array.book_genres).toMutableList()
        genres.add(0, "Selecciona un género")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            genres
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerGenre.adapter = adapter

        binding.btnListBook.setOnClickListener {
            val title = binding.etBookTitle.text.toString().trim()
            val author = binding.etAuthor.text.toString().trim()
            val genre = binding.spinnerGenre.selectedItem?.toString() ?: ""
            val synopsis = binding.etDescription.text.toString().trim()
            val pages = binding.etPages.text.toString().trim().ifEmpty { "—" }
            val language = binding.etLanguage.text.toString().trim().ifEmpty { "—" }

            if (title.isEmpty()) {
                binding.etBookTitle.error = "Ingresa el título del libro"
                return@setOnClickListener
            }
            if (author.isEmpty()) {
                binding.etAuthor.error = "Ingresa el autor"
                return@setOnClickListener
            }
            if (genre == "Selecciona un género") {
                Toast.makeText(requireContext(), "Selecciona un género", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val book = Book(
                id = 0,
                title = title,
                author = author,
                year = "2024",
                pages = pages,
                language = language,
                ownerName = "Yo",
                genre = genre,
                color = "#9C2F1F",
                synopsis = synopsis,
                ownerInitials = "YO",
                imageUri = selectedCoverUri
            )
            LocalDataStore.addBook(book)
            Toast.makeText(requireContext(), "¡Libro publicado!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }

        return binding.root
    }

    private fun setupConditionChips() {
        val chips = listOf(
            binding.chipExcellent to "Excellent",
            binding.chipGood      to "Good",
            binding.chipFair      to "Fair"
        )
        chips.forEach { (chip, condition) ->
            chip.setOnClickListener {
                selectedCondition = condition
                chips.forEach { (c, _) ->
                    c.setBackgroundResource(R.drawable.bg_condition_unselected)
                }
                chip.setBackgroundResource(R.drawable.bg_condition_selected)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        _binding = null
    }
}
