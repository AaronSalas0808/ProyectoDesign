package com.example.proyecto.ui.discovery

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.proyecto.R
import com.example.proyecto.databinding.FragmentBookInfoBinding
import com.example.proyecto.network.BookRepository
import kotlinx.coroutines.launch

class BookInfoFragment : Fragment() {

    private var _binding: FragmentBookInfoBinding? = null
    private val binding get() = _binding!!

    private var currentBook: Book? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookInfoBinding.inflate(inflater, container, false)

        bindFromArguments()
        setupClicks()
        loadBookFromApiIfPossible()

        return binding.root
    }

    private fun bindFromArguments() {
        val args = arguments

        val book = Book(
            id = args?.getString("bookId").orEmpty(),
            title = args?.getString("bookTitle").orEmpty(),
            author = args?.getString("bookAuthor").orEmpty(),
            year = args?.getString("bookYear").orEmpty(),
            pages = args?.getString("bookPages").orEmpty(),
            language = args?.getString("bookLanguage").orEmpty(),
            ownerName = args?.getString("ownerName").orEmpty(),
            synopsis = args?.getString("bookSynopsis").orEmpty(),
            imageUri = args?.getParcelable<Uri>("bookImageUri"),
            coverUrl = args?.getString("bookImageUrl")
        )

        currentBook = book
        bindBook(book)
    }

    private fun loadBookFromApiIfPossible() {
        val id = currentBook?.id.orEmpty()

        if (id.isBlank() || id.startsWith("local-")) {
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val apiBook = BookRepository.getBookById(id)
                currentBook = apiBook
                bindBook(apiBook)
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "No se pudo actualizar el libro",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun bindBook(book: Book) {
        binding.tvBookTitle.text = book.title
        binding.tvAuthor.text = book.author
        binding.tvYear.text = book.year
        binding.tvOwnerName.text = book.ownerName

        binding.tvPages.text = if (book.pages.isNotBlank()) {
            "${book.pages} Pages"
        } else {
            ""
        }

        binding.tvLanguage.text = book.language

        binding.tvStory.text = if (book.synopsis.isNotBlank()) {
            book.synopsis
        } else {
            "No synopsis available."
        }

        val imageSource: Any = book.imageUri
            ?: book.getBestRemoteImageUrl()
            ?: R.drawable.placeholder_book_cover

        binding.ivBookCover.load(imageSource) {
            crossfade(true)
            placeholder(R.drawable.placeholder_book_cover)
            error(R.drawable.placeholder_book_cover)
        }
    }

    private fun setupClicks() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        val ownerClickListener = View.OnClickListener {
            val book = currentBook ?: return@OnClickListener

            val bundle = Bundle().apply {
                putString("ownerName", book.ownerName)
            }

            findNavController().navigate(
                R.id.action_book_info_to_profile_owner,
                bundle
            )
        }

        binding.ivOwnerProfile.setOnClickListener(ownerClickListener)
        binding.tvOwnerName.setOnClickListener(ownerClickListener)

        binding.btnRequestLoan.setOnClickListener {
            val book = currentBook ?: return@setOnClickListener

            val bundle = Bundle().apply {
                putString("ownerName", book.ownerName)
                putString(
                    "defaultMessage",
                    "Hola estoy interesado en poder leer ${book.title} ¿se encuentra disponible?"
                )
            }

            findNavController().navigate(
                R.id.action_book_info_to_chat,
                bundle
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}