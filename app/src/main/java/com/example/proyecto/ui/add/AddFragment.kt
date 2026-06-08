package com.example.proyecto.ui.add

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentAddBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddViewModel by viewModels()

    private var selectedCondition: String = "Excellent"
    private var selectedCoverUri: Uri? = null

    private val pickCoverPhoto =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
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

        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.GONE

        setupCloseButton()
        setupCoverPicker()
        setupConditionChips()
        setupGenreSpinner()
        setupObservers()
        setupSubmitButton()

        return binding.root
    }

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCoverPicker() {
        binding.coverUploadContainer.setOnClickListener {
            pickCoverPhoto.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }

    private fun setupGenreSpinner() {
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
    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            binding.btnListBook.isEnabled = !loading
            binding.btnListBook.text = if (loading) {
                "Publicando..."
            } else {
                "List Book"
            }
        }

        viewModel.success.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(
                    requireContext(),
                    "¡Libro publicado!",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigateUp()
                viewModel.clearSuccess()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    error,
                    Toast.LENGTH_LONG
                ).show()

                viewModel.clearError()
            }
        }
    }

    private fun setupSubmitButton() {
        binding.btnListBook.setOnClickListener {
            val title = binding.etBookTitle.text.toString().trim()
            val author = binding.etAuthor.text.toString().trim()
            val genre = binding.spinnerGenre.selectedItem?.toString().orEmpty()
            val synopsis = binding.etDescription.text.toString().trim()
            val pagesText = binding.etPages.text.toString().trim()
            val language = binding.etLanguage.text.toString().trim()

            if (title.isBlank()) {
                binding.etBookTitle.error = "Ingresa el título del libro"
                binding.etBookTitle.requestFocus()
                return@setOnClickListener
            }

            if (author.isBlank()) {
                binding.etAuthor.error = "Ingresa el autor"
                binding.etAuthor.requestFocus()
                return@setOnClickListener
            }

            if (genre.isBlank() || genre == "Selecciona un género") {
                Toast.makeText(
                    requireContext(),
                    "Selecciona un género",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val pages = pagesText.toIntOrNull() ?: 0

            viewModel.publishBook(
                title = title,
                author = author,
                pages = pages,
                language = language.ifBlank { "Unknown" },
                genre = genre,
                synopsis = synopsis,
                condition = selectedCondition,
                localImageUri = selectedCoverUri
            )
        }
    }

    private fun setupConditionChips() {
        val chips = listOf(
            binding.chipExcellent to "Excellent",
            binding.chipGood to "Good",
            binding.chipFair to "Fair"
        )

        chips.forEach { (chip, condition) ->
            chip.setOnClickListener {
                selectedCondition = condition

                chips.forEach { (itemChip, _) ->
                    itemChip.setBackgroundResource(R.drawable.bg_condition_unselected)
                }

                chip.setBackgroundResource(R.drawable.bg_condition_selected)
            }
        }

        binding.chipExcellent.setBackgroundResource(R.drawable.bg_condition_selected)
    }

    override fun onDestroyView() {
        requireActivity()
            .findViewById<BottomNavigationView>(R.id.nav_view)
            ?.visibility = View.VISIBLE

        super.onDestroyView()
        _binding = null
    }
}