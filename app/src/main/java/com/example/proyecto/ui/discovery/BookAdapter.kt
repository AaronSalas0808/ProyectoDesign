package com.example.proyecto.ui.discovery

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.proyecto.R
import com.example.proyecto.databinding.BookCardBinding

class BookAdapter(
    private var books: List<Book>,
    private val onBookClick: (Book) -> Unit = {},
    private val onOwnerClick: (Book) -> Unit = {}
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    class BookViewHolder(val binding: BookCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BookViewHolder {
        val binding = BookCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BookViewHolder,
        position: Int
    ) {
        val book = books[position]
        val imageUrl = book.getBestRemoteImageUrl()

        with(holder.binding) {
            tvBookTitle.text = book.title
            tvBookAuthor.text = book.author
            tvOwnerName.text = book.ownerName

            Log.d(
                "BookAdapter",
                "Render title=${book.title} owner=${book.ownerName} image=$imageUrl"
            )

            if (book.imageUri != null) {
                ivBookCover.load(book.imageUri) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_book_cover)
                    error(R.drawable.placeholder_book_cover)
                }
            } else if (!imageUrl.isNullOrBlank()) {
                ivBookCover.load(imageUrl) {
                    crossfade(true)
                    placeholder(R.drawable.placeholder_book_cover)
                    error(R.drawable.placeholder_book_cover)
                    listener(
                        onError = { _, result ->
                            Log.e(
                                "BookAdapter",
                                "Error cargando imagen ${book.title}: ${result.throwable.message}"
                            )
                        }
                    )
                }
            } else {
                ivBookCover.setImageResource(R.drawable.placeholder_book_cover)
            }

            btnViewBook.setOnClickListener {
                onBookClick(book)
            }

            ivOwnerProfile.setOnClickListener {
                onOwnerClick(book)
            }

            tvOwnerName.setOnClickListener {
                onOwnerClick(book)
            }

            root.setOnClickListener {
                onBookClick(book)
            }
        }
    }

    override fun getItemCount(): Int = books.size

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }
}