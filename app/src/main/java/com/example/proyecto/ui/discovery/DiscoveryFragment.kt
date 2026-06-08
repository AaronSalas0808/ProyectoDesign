package com.example.proyecto.ui.discovery

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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

    private lateinit var viewModel: DiscoveryViewModel
    private lateinit var bookAdapter: BookAdapter

    private var allBooks: List<Book> = emptyList()
    private var filteredBooks: List<Book> = emptyList()

    private var selectedGenre: String = "All"
    private var searchQuery: String = ""

    private var currentPage: Int = 1
    private val booksPerPage: Int = 8

    private var tvPageInfo: TextView? = null
    private var btnPreviousPage: View? = null
    private var btnNextPage: View? = null
    private var tvEmptyState: TextView? = null
    private var tvLoadingState: TextView? = null

    private val genres = listOf(
        "All",
        "Fiction",
        "Science",
        "History",
        "Philosophy",
        "Technology",
        "Art"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[DiscoveryViewModel::class.java]

        _binding = FragmentDiscoveryBinding.inflate(inflater, container, false)

        setupOptionalViews()
        setupToolbar()
        setupRecycler()
        setupSearch()
        setupGenreChips()
        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadBooks()
    }

    private fun setupOptionalViews() {
        tvPageInfo = findViewByName("tvPageInfo")
        btnPreviousPage = findViewByName("btnPreviousPage")
        btnNextPage = findViewByName("btnNextPage")
        tvEmptyState = findViewByName("tvEmptyState")
        tvLoadingState = findViewByName("tvLoadingState")

        btnPreviousPage?.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                renderCurrentPage()
            }
        }

        btnNextPage?.setOnClickListener {
            val totalPages = getTotalPages()

            if (currentPage < totalPages) {
                currentPage++
                renderCurrentPage()
            }
        }
    }

    private fun setupToolbar() {
        binding.ivMenuBurger.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }

    private fun setupRecycler() {
        bookAdapter = BookAdapter(
            books = emptyList(),
            onBookClick = { book ->
                openBookInfo(book)
            },
            onOwnerClick = { book ->
                openOwnerProfile(book)
            }
        )

        binding.rvBooks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBooks.adapter = bookAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) = Unit

            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString().orEmpty()
                currentPage = 1
                applyFilter()
            }
        })
    }

    private fun setupGenreChips() {
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

                setOnClickListener {
                    selectedGenre = genre
                    currentPage = 1
                    refreshChipStyles(container)
                    applyFilter()
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = if (genre == genres.first()) 0 else dpToPx(10)
            }

            chip.layoutParams = params
            container.addView(chip)
        }

        refreshChipStyles(container)
    }

    private fun refreshChipStyles(container: LinearLayout) {
        for (i in 0 until container.childCount) {
            val chip = container.getChildAt(i) as? TextView ?: continue
            val isSelected = chip.text.toString() == selectedGenre
            applyChipStyle(chip, isSelected)
        }
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

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            tvLoadingState?.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Error: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        viewModel.books.observe(viewLifecycleOwner) { books ->
            allBooks = books
            currentPage = 1
            applyFilter()
        }
    }

    private fun applyFilter() {
        filteredBooks = allBooks
            .filter { book ->
                selectedGenre == "All" ||
                        book.genre.equals(selectedGenre, ignoreCase = true)
            }
            .filter { book ->
                searchQuery.isBlank() ||
                        book.title.contains(searchQuery, ignoreCase = true) ||
                        book.author.contains(searchQuery, ignoreCase = true)
            }

        renderCurrentPage()
    }

    private fun renderCurrentPage() {
        val totalPages = getTotalPages()

        if (currentPage > totalPages) {
            currentPage = totalPages
        }

        if (currentPage < 1) {
            currentPage = 1
        }

        val fromIndex = ((currentPage - 1) * booksPerPage)
            .coerceAtLeast(0)
            .coerceAtMost(filteredBooks.size)

        val toIndex = (fromIndex + booksPerPage)
            .coerceAtMost(filteredBooks.size)

        val pageBooks = if (filteredBooks.isEmpty()) {
            emptyList()
        } else {
            filteredBooks.subList(fromIndex, toIndex)
        }

        bookAdapter.updateBooks(pageBooks)

        tvPageInfo?.text = "Page $currentPage of $totalPages"

        btnPreviousPage?.isEnabled = currentPage > 1
        btnPreviousPage?.alpha = if (currentPage > 1) 1f else 0.45f

        btnNextPage?.isEnabled = currentPage < totalPages
        btnNextPage?.alpha = if (currentPage < totalPages) 1f else 0.45f

        tvEmptyState?.visibility =
            if (filteredBooks.isEmpty()) View.VISIBLE else View.GONE

        binding.rvBooks.visibility =
            if (filteredBooks.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun getTotalPages(): Int {
        if (filteredBooks.isEmpty()) return 1

        return kotlin.math.ceil(
            filteredBooks.size / booksPerPage.toDouble()
        ).toInt().coerceAtLeast(1)
    }

    private fun openBookInfo(book: Book) {
        val bundle = Bundle().apply {
            putString("bookId", book.id)
            putString("bookTitle", book.title)
            putString("bookAuthor", book.author)
            putString("bookYear", book.year)
            putString("bookPages", book.pages)
            putString("bookLanguage", book.language)
            putString("ownerName", book.ownerName)
            putString("bookSynopsis", book.synopsis)
            putParcelable("bookImageUri", book.imageUri)
            putString("bookImageUrl", book.getBestRemoteImageUrl())
        }

        findNavController().navigate(
            R.id.action_discovery_to_book_info,
            bundle
        )
    }

    private fun openOwnerProfile(book: Book) {
        val bundle = Bundle().apply {
            putString("ownerName", book.ownerName)
        }

        findNavController().navigate(
            R.id.action_discovery_to_profile_owner,
            bundle
        )
    }

    private fun <T : View> findViewByName(idName: String): T? {
        val id = resources.getIdentifier(
            idName,
            "id",
            requireContext().packageName
        )

        return if (id != 0) {
            binding.root.findViewById(id)
        } else {
            null
        }
    }

    private fun dpToPx(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}