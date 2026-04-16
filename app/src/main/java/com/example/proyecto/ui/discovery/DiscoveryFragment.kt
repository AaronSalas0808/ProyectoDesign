package com.example.proyecto.ui.discovery

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyecto.MainActivity
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentDiscoveryBinding

class DiscoveryFragment : Fragment() {

    private var _binding: FragmentDiscoveryBinding? = null
    private val binding get() = _binding!!

    private var allBooks: List<Book> = emptyList()
    private var selectedGenre: String = "Todos"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val viewModel = ViewModelProvider(this)[DiscoveryViewModel::class.java]

        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)

        binding.ivMenuBurger.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }

        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())

        setupGenreChips()

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                android.widget.Toast.makeText(
                    requireContext(), "Error: $error", android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.books.observe(viewLifecycleOwner) { books ->
            allBooks = books
            applyFilter()
        }

        return binding.root
    }

    private fun setupGenreChips() {
        val genres = listOf("Todos") + resources.getStringArray(R.array.book_genres).toList()
        val container = binding.chipContainer
        container.removeAllViews()

        genres.forEach { genre ->
            val chip = TextView(requireContext()).apply {
                text = genre
                textSize = 13f
                setTypeface(null, Typeface.BOLD)
                gravity = Gravity.CENTER
                setPadding(dpToPx(16), 0, dpToPx(16), 0)
                height = dpToPx(36)
                isClickable = true
                isFocusable = true
                setOnClickListener { selectChip(genre, this, container) }
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginStart = if (genre == "Todos") 0 else dpToPx(10) }
            chip.layoutParams = params
            applyChipStyle(chip, genre == "Todos")
            container.addView(chip)
        }
    }

    private fun selectChip(genre: String, selected: TextView, container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val chip = container.getChildAt(i) as? TextView ?: continue
            applyChipStyle(chip, chip == selected)
        }
        selectedGenre = genre
        applyFilter()
    }

    private fun applyChipStyle(chip: TextView, isSelected: Boolean) {
        if (isSelected) {
            chip.setBackgroundResource(R.drawable.bg_chip_selected)
            chip.setTextColor(0xFFFFFFFF.toInt())
        } else {
            chip.setBackgroundResource(R.drawable.bg_chip_info)
            chip.setTextColor(0xFF757575.toInt())
        }
    }

    private fun applyFilter() {
        val filtered = if (selectedGenre == "Todos") allBooks
        else allBooks.filter { it.genre.equals(selectedGenre, ignoreCase = true) }

        binding.rvBooks.adapter = BookAdapter(
            books = filtered,
            onBookClick = { book ->
                val bundle = Bundle().apply {
                    putString("bookTitle", book.title)
                    putString("bookAuthor", book.author)
                    putString("bookYear", book.year)
                    putString("bookPages", book.pages)
                    putString("bookLanguage", book.language)
                    putString("ownerName", book.ownerName)
                }
                findNavController().navigate(R.id.action_discovery_to_book_info, bundle)
            },
            onOwnerClick = { book ->
                val bundle = Bundle().apply { putString("ownerName", book.ownerName) }
                findNavController().navigate(R.id.action_discovery_to_profile_owner, bundle)
            }
        )
    }

    private fun dpToPx(dp: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), resources.displayMetrics
    ).toInt()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
